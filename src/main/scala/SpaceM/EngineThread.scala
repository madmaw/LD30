package SpaceM

import java.awt.{Font, Color, Graphics, Graphics2D}
import java.awt.image.BufferedImage
import javax.swing.SwingUtilities

import SpaceM.State.State

/**
 * Created by chris on 23/08/14.
 */
class EngineThread(var currentState: State, val buffer: BufferedImage, val graphics: Graphics) extends Thread {

  var running: Boolean = true;
  var width = this.buffer.getWidth
  var height = this.buffer.getHeight

  def requestStop(): Unit = {
    this.running = false;
  }

  override def run(): Unit = {
    val bufferGraphics = this.buffer.getGraphics

    val renderer = new Runnable {
      override def run(): Unit = {
        graphics.drawImage(buffer, 0, 0, null)
      }
    }
    bufferGraphics match {
      case bufferGraphics2D: Graphics2D =>
        var age = 0L
        var frames = 0
        var now = System.currentTimeMillis()
        this.currentState.init()
        bufferGraphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, height / 20))
        while (this.running && this.currentState != null) {

          this.currentState.render(bufferGraphics2D, this.width, this.height);

          // FPS
//          if( age > 1000) {
//            bufferGraphics2D.setColor(Color.PINK)
//            bufferGraphics2D.drawString((frames/(age / 1000)).toString, 0, 50)
//          }

          SwingUtilities.invokeAndWait(renderer);

          val next = System.currentTimeMillis()
          val delta = next - now
          age += delta;
          frames += 1
          val state = this.currentState.update(age, delta, this.width, this.height);
          if (state != this.currentState) {
            age = 0L
            frames = 0
            if( state != null ) {
              state.init();
            }
            this.currentState = state
          }
          now = next
        }
      case _ =>
        // it's seriously fucked
        graphics.setColor(Color.RED)
        graphics.fillRect(0, 0, this.width, this.height)
    }

  }

}
