package under

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Vector2
import scalaz.syntax.std.boolean._
import ammonite.ops._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer

class BaseActor(
  texture:Texture,
  _x:Float,
  _y:Float
) extends Actor{
  val region = new TextureRegion(texture)
  setX(_x)
  setY(_y)
  setWidth(texture.getWidth)
  setHeight(texture.getHeight)
  setOriginX(texture.getWidth/2)
  setOriginY(texture.getHeight/2)
  var velocity = new Vector2(0, 0)
  def position:Vector2 = new Vector2(getX, getY)
  def boundary  = new Rectangle(getX, getY, getWidth, getHeight)
  
  def collidedWith(e:Rectangle):Boolean = boundary.overlaps(e) 
  def collidedWith(e:Entity):Boolean = boundary.overlaps(e.boundary) 
  override def act(delta: Float):Unit = {
    super.act(delta)
    moveBy(velocity.x * delta, velocity.y * delta)
  }

  override def draw(batch:Batch, parentAlpha:Float):Unit = 
    batch.draw(region, getX, getY, getOriginX, getOriginY, getWidth, getHeight, getScaleX, getScaleY, getRotation)
  
}

class Entity(
  texture:Texture,
  _x:Float,
  _y:Float,
  _goal:Vector2
) extends BaseActor(texture, _x,_y){
  private val self = this
  val goalX = _x + _goal.x
  val goalY = _y + _goal.y
  def reverseMovement: Entity = {
    withVelocity(new Vector2(velocity.x * -1, velocity.y * -1))
  }
  def goal:Vector2 = new Vector2(goalX - getX, goalY - getY)
  def withVelocity(v:Vector2) = {
    velocity = v
    this
  }


}

object Entity{
    def apply(position:Vector2, file:FileHandle, goal:Vector2) = {
      val e = new Entity(
        texture = new Texture(file),
        _x = position.x,
        _y = position.y,
        _goal = goal
      )
      e.addAction(Actions.forever(Actions.rotateBy(360,1)))
      e
    }
  }
case class Thirds(top:Rectangle, middle:Rectangle, bottom:Rectangle)
class Underpressure extends Game {

    val entitySize = 60
    val internalWidth = 100

    lazy val mainStage = new Stage()
    lazy val frame = new BaseActor(new Texture("frame-small.png"), 0, 0)

    lazy val floor = new BaseActor(new Texture("gray.png"), 0, 0)

    lazy val entity1 = Entity(
      position = frame.boundary 
                |> livingSpace 
                |> horizontalThirds
                |> (_.top)
                |> verticalThirds
                |> (_.middle)
                |> leftPosition,
      file = Gdx.files.internal("sprocket-1-small.png"),
      goal = new Vector2(300, -400)
      )

    lazy val entity2 = Entity(
      position = frame.boundary 
                |> livingSpace 
                |> horizontalThirds
                |> (_.bottom)
                |> verticalThirds
                |> (_.middle)
                |> leftPosition,
      file = Gdx.files.internal("sprocket-2-small.png"),
      goal = new Vector2(300, 600)
      )

    def topBorder(frame:Rectangle):Rectangle = 
      new Rectangle(frame.getX , frame.getHeight - internalWidth,  frame.getWidth, internalWidth)

    def bottomBorder(frame:Rectangle):Rectangle = 
      new Rectangle(frame.getX , frame.getY,  frame.getWidth, internalWidth)

    def rightBorder(frame:Rectangle):Rectangle = 
      new Rectangle(frame.getX, frame.getY,  internalWidth, frame.getHeight)

    def leftBorder(frame:Rectangle):Rectangle = 
      new Rectangle(frame.getWidth  - internalWidth , frame.getY,  internalWidth, frame.getHeight)


    def horizontalThirds(frame:Rectangle):Thirds = Thirds(
        bottom = new Rectangle(frame.getX, frame.getY, frame.getWidth, frame.getHeight/3),
        middle = new Rectangle(frame.getX, frame.getY + frame.getHeight/3, frame.getWidth, frame.getHeight/3),
        top    = new Rectangle(frame.getX, frame.getY + 2 * frame.getHeight/3, frame.getWidth, frame.getHeight/3)
      )

    def verticalThirds(frame:Rectangle):Thirds = Thirds(
        bottom = new Rectangle(frame.getX, frame.getY, frame.getWidth/3, frame.getHeight),
        middle = new Rectangle(frame.getX + frame.getWidth/3, frame.getY, frame.getWidth/3, frame.getHeight),
        top    = new Rectangle(frame.getX + 2 * frame.getWidth/3, frame.getY, frame.getWidth/3, frame.getHeight)
      )

    def randomPosition(frame:Rectangle):Vector2 = new Vector2(frame.getX, frame.getY)
    def leftPosition(frame:Rectangle):Vector2 = new Vector2(frame.getX, frame.getY)
    def rightPosition(frame:Rectangle):Vector2 = new Vector2(frame.getWidth, frame.getY)

    def  livingSpace(frame:Rectangle) = new Rectangle(
      frame.getX + internalWidth, 
      frame.getY + internalWidth, 
      frame.getWidth - internalWidth * 3/2, 
      frame.getHeight - internalWidth * 2
    )

    lazy val entities = ListBuffer(entity1, entity2)

    override def create():Unit =  {
      mainStage.addActor(floor)
      mainStage.addActor(frame)
      entities.foreach(mainStage.addActor)
      }

    def collidedWithWithWalls(entity:Entity):Boolean = 
      (
        entity.collidedWith(frame.boundary |> topBorder) ||
          entity.collidedWith(frame.boundary |> bottomBorder) ||
          entity.collidedWith(frame.boundary |> rightBorder) ||
          entity.collidedWith(frame.boundary |> leftBorder) 
      )

    override def render():Unit =  {
     entities.foreach{
       case entity if entity.goal.isZero(20) =>
            entity.withVelocity(Vector2.Zero)
       case entity if collidedWithWithWalls(entity) || entities.filterNot(_ == entity).exists(entity.collidedWith)=>
         entity.velocity.scl(-1.3f)
       case entity =>
            val avoiding = entities.filterNot(_ == entity).collect{
              case otherEntity if entity.position.dst(otherEntity.position) <= internalWidth * 2  =>
                  (v:Vector2) => v.rotate(0.75f)
              }
            if (avoiding.nonEmpty) avoiding.foldLeft(entity.velocity)((v, f) => f(v))
            else entity.withVelocity(entity.goal.cpy.nor.scl(100, 100))
      }

      mainStage.act(Gdx.graphics.getDeltaTime)

      Gdx.gl.glClearColor(1,1,1,1)
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

      mainStage.draw()
    }
}
