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
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import ammonite.ops._


case class Entity(
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
  def collided(e:Rectangle):Boolean = boundary.overlaps(e)
  def reverseMovement(): Unit = {
    velocity.x = velocity.x * -1
    velocity.y = velocity.y * -1
  }
  override def act(delta: Float):Unit = {
    super.act(delta)
    moveBy(velocity.x * delta, velocity.y * delta)
  }
  override def draw(batch:Batch, parentAlpha:Float):Unit = 
    batch.draw(region, getX, getY, getOriginX, getOriginY, getWidth, getHeight, getScaleX, getScaleY, getRotation)
  
}
object Entity{
    def apply(position:Vector2, file:FileHandle) = {
      val e = new Entity(
        texture = new Texture(file),
        _x = position.x,
        _y = position.y
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
    lazy val frame = new Entity(new Texture("frame-small.png"), 0, 0)

    lazy val floor = new Entity(new Texture("gray.png"), 0, 0)


    lazy val entity1 = Entity(
      position = frame.boundary 
                |> livingSpace 
                |> horizontalThirds 
                |> (_.bottom) 
                |> verticalThirds 
                |> (_.top) 
                |> randomPosition,
      file = Gdx.files.internal("sprocket-1-small.png")
      )

    lazy val entity2 = Entity(
      position = frame.boundary 
                |> livingSpace 
                |> horizontalThirds 
                |> (_.bottom) 
                |> verticalThirds 
                |> (_.bottom) 
                |> randomPosition,
      file = Gdx.files.internal("sprocket-2-small.png")
      )

    lazy val entities = List(entity1, entity2)

    def topBorder(frame:Rectangle):Rectangle = 
      new Rectangle(frame.getX , frame.getHeight - internalWidth,  frame.getWidth, internalWidth)

    def bottomBorder(frame:Rectangle):Rectangle = 
      new Rectangle(frame.getX , frame.getY,  frame.getWidth, internalWidth)

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

    def randomPosition(frame:Rectangle):Vector2 = new Vector2(frame.getX + 1, frame.getY + 1)

    def  livingSpace(frame:Rectangle) = new Rectangle(
      frame.getX + internalWidth, 
      frame.getY + internalWidth, 
      frame.getWidth - internalWidth, 
      frame.getHeight - internalWidth * 2
    )

    override def create():Unit =  {
      mainStage.addActor(floor)
      mainStage.addActor(frame)
      mainStage.addActor(entity1)
      mainStage.addActor(entity2)
      println(frame.boundary |> topBorder)
      println(entity1.position)
      println(entity1.collided(frame.boundary |> topBorder))
      }

    var started = false
    override def render():Unit =  {
      started = Gdx.input.isKeyPressed(Keys.ENTER)
      if (started) entity1.velocity = new Vector2(0, -1 * mainStage.getWidth / 7)
      if (entity1.collided(frame.boundary |> topBorder)) entity1.reverseMovement()
      if (entity1.collided(frame.boundary |> bottomBorder)) entity1.reverseMovement()
      mainStage.act(Gdx.graphics.getDeltaTime)

      Gdx.gl.glClearColor(1,1,1,1)
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

      mainStage.draw()
    }
}
