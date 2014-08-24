package SpaceM

import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Component, Graphics2D}

import SpaceM.State.State

/**
 * Created by chris on 23/08/14.
 */
class Engine(val initialState: State, val canvas: Component) {

  var thread: EngineThread = null;


  def start(): Unit = {
    if( this.thread != null ) {
      this.thread.requestStop()
    }
    val graphics = canvas.getGraphics();
    graphics match {
      case graphics2D: Graphics2D =>
        val buffer = new BufferedImage(this.canvas.getWidth, this.canvas.getHeight, BufferedImage.TYPE_INT_ARGB)
        this.thread = new EngineThread(
          this.initialState,
          buffer,
          graphics2D
        );
        this.thread.start()
      case _ =>
        throw new RuntimeException("no graphics 2d!")
    }

  }

  def stop(): Unit = {
    if( this.thread != null ) {
      this.thread.requestStop()
      this.thread = null
    }
  }

}
