package SpaceM.State.Level

import java.awt.{Point, Image, Graphics2D}

/**
 * Created by chris on 24/08/14.
 */
class PowerUpRenderer(
  image: Image,
  imageCenter: Point
) extends MonsterRenderer {

  override def render(graphics: Graphics2D, monster: Monster): Unit = {
    monster.activity match {
      case projectileActivity: ProjectileActivity =>
        val rotation = projectileActivity.rotationRadiansPerMilli * monster.activityAge
        val copy = graphics.create()
        copy match {
          case copy2D: Graphics2D =>
            copy2D.rotate(rotation, monster.positionX, monster.positionY)
            copy2D.drawImage(image, monster.positionX - imageCenter.x, monster.positionY - imageCenter.y, null)
        }
        copy.dispose()
    }
  }
}
