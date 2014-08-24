package SpaceM.State.Level

import java.awt.{Color, Graphics2D}

/**
 * Created by chris on 23/08/14.
 */
class CircleRenderer(
  val color: Color
) extends MonsterRenderer {
  override def render(graphics: Graphics2D, monster: Monster): Unit = {
    graphics.setColor(this.color)
    graphics.drawOval(
      monster.positionX - monster.radius,
      monster.positionY - monster.radius,
      monster.radius * 2,
      monster.radius * 2
    )
  }
}
