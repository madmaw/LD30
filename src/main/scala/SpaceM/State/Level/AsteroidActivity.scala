package SpaceM.State.Level

import java.awt.Point
import java.awt.geom.Point2D

import scala.collection.mutable.ArrayBuffer

/**
 * Created by chris on 24/08/14.
 */
class AsteroidActivity(
  rotationRadiansPerMilli: Double,
  angle: Double,
  pixelsPerMilli: Double,
  toughness: Int,
  var unscaledPoints: ArrayBuffer[Point2D],
  val contents: Monster
) extends ProjectileActivity(rotationRadiansPerMilli, angle, pixelsPerMilli, toughness) {

  // create some render hints
  if( unscaledPoints == null ) {
    unscaledPoints = new ArrayBuffer[Point2D]()
    var a = Math.random() * Math.PI
    val end = a + Math.PI * 2
    while( a < end ) {
      val sin = Math.sin(a);
      val cos = Math.cos(a);
      val r = Math.random() * 0.2 + 0.96
      val x = cos * r
      val y = sin * r
      unscaledPoints += new Point2D.Double(x, y)
      a += Math.random() * Math.PI / 8 + Math.PI/12
    }
  }

}
