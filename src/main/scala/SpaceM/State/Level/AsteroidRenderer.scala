package SpaceM.State.Level

import java.awt.{Polygon, Paint, Color, Graphics2D}

/**
 * Created by chris on 24/08/14.
 */
class AsteroidRenderer extends MonsterRenderer {

  val fillColor = new Color(0xff, 0xff, 0xff, 0xaa);
  val outlinePaint = Color.WHITE

  override def render(graphics: Graphics2D, monster: Monster): Unit = {
    val activity = monster.activity
    activity match {
      case asteroidActivity: AsteroidActivity =>
        val rotation = monster.activityAge * asteroidActivity.rotationRadiansPerMilli
        val copy = graphics.create()
        copy match {
          case copy2D: Graphics2D  =>
            copy2D.rotate(rotation, monster.positionX, monster.positionY)
            val polygon = new Polygon()
            for( p <- asteroidActivity.unscaledPoints ) {
              polygon.addPoint(monster.positionX + (p.getX * monster.radius).toInt, monster.positionY + (p.getY * monster.radius).toInt)
            }

            var fill = fillColor;
            var outline = outlinePaint;
            if( monster.monsterType == MonsterType.Fragment ) {
              val proportion = 1 - (monster.activityAge.toDouble / 1000)
              fill = new Color(fill.getRed, fill.getGreen, fill.getBlue, (fill.getAlpha * proportion).toInt)
              outline = new Color(outline.getRed, outline.getGreen, outline.getBlue, (outline.getAlpha * proportion).toInt)
            }

            copy2D.setPaint(fill)
            copy2D.fillPolygon(polygon)
            copy2D.setPaint(outline)
            copy2D.drawPolygon(polygon)
        }
        copy.dispose()
    }
  }

}
