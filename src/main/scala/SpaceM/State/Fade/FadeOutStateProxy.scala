package SpaceM.State.Fade

import java.awt.{Color, Graphics2D}

import SpaceM.State.State

/**
 * Created by chris on 24/08/14.
 */
class FadeOutStateProxy(
  proxied: State,
  fadeOutTimeMillis: Long
) extends State {

  var nextState: State = null;
  var nextStateAge: Long = 0;

  override def init(): Unit = {
    this.nextState = null
    this.proxied.init();
  }

  override def update(age: Long, delta: Long, width: Int, height: Int): State = {
    var result: State = this
    val next = this.proxied.update(age, delta, width, height)
    if( next == proxied ) {
      // undo the fade out
      this.nextState = null;
    }
    if( next != this.proxied && this.nextState == null ) {
      this.nextState = next
      this.nextStateAge = 0
    }
    if( nextState != null ) {
      if( nextStateAge > fadeOutTimeMillis ) {
        result = nextState
      } else {
        this.nextStateAge += delta
      }
    }
    return result;
  }

  override def render(graphics: Graphics2D, width: Int, height: Int): Unit = {
    this.proxied.render(graphics, width, height)
    if( this.nextState != null ) {
      val alpha = Math.min(255, Math.max(0, (255 * nextStateAge) / fadeOutTimeMillis)).toInt
      graphics.setColor(new Color(0, 0, 0, alpha))
      graphics.fillRect(0, 0, width, height)
    }
  }
}
