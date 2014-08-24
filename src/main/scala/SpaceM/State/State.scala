package SpaceM.State

import java.awt.Graphics2D

/**
 * Created by chris on 23/08/14.
 */
trait State {

  def init();

  def update(age: Long, delta: Long, width: Int, height: Int): State;

  def render(graphics: Graphics2D, width: Int, height: Int);

}
