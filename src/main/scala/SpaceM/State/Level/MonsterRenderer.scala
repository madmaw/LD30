package SpaceM.State.Level

import java.awt.Graphics2D

/**
 * Created by chris on 23/08/14.
 */
trait MonsterRenderer {
  def render(graphics: Graphics2D, monster: Monster)
}
