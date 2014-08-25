package SpaceM.State.Level

import java.awt.{Point, Image, Graphics2D}

/**
 * Created by chris on 24/08/14.
 */
class BulletRenderer(
  images: Array[Image],
  centerPoint: Point,
  millisPerFrame: Long
) extends MonsterRenderer {

  override def render(graphics: Graphics2D, monster: Monster): Unit = {
    monster.activity match {
      case projectileActivity: ProjectileActivity =>
        val copy = graphics.create()
        copy match {
          case copy2D: Graphics2D =>
            val index = ((monster.activityAge / millisPerFrame) % images.length).toInt
            val image = images(index);
            if( monster.monsterType != MonsterType.Jelly ) {
              copy2D.rotate(projectileActivity.angle, monster.positionX, monster.positionY)
              copy2D.drawImage(image, monster.positionX - centerPoint.x, monster.positionY - centerPoint.y, null)
            } else {
              copy2D.drawImage(
                image,
                (monster.positionX - monster.radius).toInt,
                (monster.positionY - monster.radius/2).toInt,
                (monster.radius * 2).toInt,
                (monster.radius * 2).toInt,
                null
              )
            }
        }
        copy.dispose()
    }
  }
}
