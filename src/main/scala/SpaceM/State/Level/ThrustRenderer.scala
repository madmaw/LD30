package SpaceM.State.Level

import java.awt.{AlphaComposite, Point, Image, Graphics2D}

/**
 * Created by chris on 24/08/14.
 */
class ThrustRenderer(
  image: Image,
  imageCenter: Point
) extends MonsterRenderer {

  override def render(graphics: Graphics2D, monster: Monster): Unit = {
    monster.activity match {
      case projectileActivity: ProjectileActivity =>
        val copy = graphics.create()
        val life = 1 - (monster.activityAge/2000.0)
        if( life > 0 ) {
          val rotation = monster.activityAge * projectileActivity.rotationRadiansPerMilli
          copy match {
            case copy2D: Graphics2D =>
              copy2D.rotate(rotation, monster.positionX, monster.positionY)
              val sw = life * image.getWidth(null)
              val sh = life * image.getHeight(null)
              val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, life.toFloat);
              copy2D.setComposite(ac);
              copy2D.drawImage(
                image,
                monster.positionX - (imageCenter.getX * life).toInt,
                monster.positionY - (imageCenter.getY * life).toInt,
                sw.toInt,
                sh.toInt,
                null
              )
          }
          copy.dispose
        }
    }
  }
}
