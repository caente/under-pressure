package under

import com.badlogic.gdx.backends.lwjgl._

object Main extends App {
    val cfg = new LwjglApplicationConfiguration
    cfg.title = "under-pressure"
    cfg.width = 1048
    cfg.height = 1440
    cfg.forceExit = false
    new LwjglApplication(new Underpressure, cfg)
}
