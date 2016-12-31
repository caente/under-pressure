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



class BaseActor(
  val texture:Texture,
  val _x:Float,
  val _y:Float
) extends Actor{
  val region = new TextureRegion(texture)
  setX(_x)
  setY(_y)
  setWidth(texture.getWidth)
  setHeight(texture.getHeight)
  var velocityX = 0f
  var velocityY = 0f
  def boundary  = {
   val r =  new Rectangle
   r.set(getX, getY, getWidth, getHeight)
   r
  }
  override def act(dt: Float):Unit = {
    super.act(dt)
    moveBy(velocityX * dt, velocityY * dt)
  }
  override def draw(batch:Batch, parentAlpha:Float):Unit = {
    batch.setColor(getColor.r, getColor.g, getColor.b, getColor.a)
    if (isVisible)
      batch.draw(region, getX, getY, getOriginX, getOriginY, getWidth, getHeight, getScaleX, getScaleY, getRotation)
  }
}

class Underpressure extends Game {

    val entitySize = 60
    val internalWidth = 100

    lazy val mainStage = new Stage()
    lazy val frame = new BaseActor(new Texture("frame-small.png"), 0, 0)

    lazy val floor = new BaseActor(new Texture("gray.png"), 0, 0)

    lazy val entity = {
         val e = new BaseActor(
                        texture = new Texture(Gdx.files.internal("sprocket-2-small.png")),
                        _x =      (mainStage.getWidth - entitySize) / 2,
                        _y =      internalWidth + 1
                )
         e.setOriginX(e.getWidth/2)
         e.setOriginY(e.getHeight/2)
         e
    }

    def  livingSpace(frame:Rectangle) = new Rectangle(
      frame.getX + internalWidth, 
      frame.getY + internalWidth, 
      frame.getWidth - internalWidth * 2, 
      frame.getHeight - internalWidth * 2
    )

    override def create():Unit =  {
      mainStage.addActor(floor)
      mainStage.addActor(frame)
      mainStage.addActor(entity)
      entity.addAction(Actions.forever(Actions.rotateBy(360,1)))
      }

    var started = false
    override def render():Unit =  {
      if (Gdx.input.isKeyPressed(Keys.ENTER)) started = true
      val speedY = 
        if (!livingSpace(frame.boundary).contains(entity.boundary) || !started)  0 
        else   mainStage.getWidth / 7

      entity.velocityY = speedY
      mainStage.act(Gdx.graphics.getDeltaTime)

      Gdx.gl.glClearColor(1,1,1,1)
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

      mainStage.draw()
    }
}
