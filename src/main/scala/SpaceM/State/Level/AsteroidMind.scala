package SpaceM.State.Level

import java.awt.geom.Point2D

import SpaceM.SoundEffect

import scala.collection.mutable.ArrayBuffer

/**
 * Created by chris on 24/08/14.
 */
class AsteroidMind(
  val explosionSoundEffect: SoundEffect
) extends ProjectileMind {

  val fragmentMind = new FragmentMind()

  override def collision(monster: Monster, collidedWith: Monster, level: LevelState): Boolean = {

    monster.activity match {
      case asteroidActivity: AsteroidActivity =>
        if( collidedWith.monsterType == MonsterType.Bullet ) {
          // spawn two weaker asteroids
          if (asteroidActivity.toughness > 1) {
            val asteroid1 = new Monster(
              MonsterType.Asteroid,
              this,
              monster.positionX,
              monster.positionY,
              monster.radius / 2,
              -1
            )
            val activity1 = new AsteroidActivity(
              asteroidActivity.rotationRadiansPerMilli,
              asteroidActivity.angle - Math.PI * Math.random() / 4,
              asteroidActivity.pixelsPerMilli,
              asteroidActivity.toughness / 2,
              null,
              null
            )
            asteroid1.activity = activity1

            val asteroid2 = new Monster(
              MonsterType.Asteroid,
              this,
              monster.positionX,
              monster.positionY,
              monster.radius / 2,
              -1
            )
            val activity2 = new AsteroidActivity(
              -asteroidActivity.rotationRadiansPerMilli,
              asteroidActivity.angle + Math.PI * Math.random() / 4,
              asteroidActivity.pixelsPerMilli,
              asteroidActivity.toughness / 2,
              null,
              null
            )
            asteroid2.activity = activity2

            level.enemies += asteroid1
            level.enemies += asteroid2


          }
          if (asteroidActivity.contents != null) {
            val contents = asteroidActivity.contents
            contents.positionX = monster.positionX
            contents.positionY = monster.positionY
            level.powerUps += contents
          }
        }
        // create some fragments
        var index = 0;
        val rotation = monster.activityAge * asteroidActivity.rotationRadiansPerMilli
        val sin = Math.sin(rotation)
        val cos = Math.cos(rotation)
        while (index < asteroidActivity.unscaledPoints.length - 1) {
          val next = Math.min((index + 2 + Math.random() * 3).toInt, asteroidActivity.unscaledPoints.length - 1)
          if (next - index > 1) {
            val parts = new ArrayBuffer[Point2D]()
            parts += new Point2D.Double(0, 0)
            var fragmentAngle = 0D;
            for (i <- index to next) {
              val point = asteroidActivity.unscaledPoints(i)
              val rx = cos * point.getX - sin * point.getY
              val ry = sin * point.getX + cos * point.getY
              if (i == index) {
                fragmentAngle = Math.atan2(rx, ry)
              }
              parts += new Point2D.Double(rx, ry);
            }

            index = next;
            val fragment = new Monster(
              MonsterType.Fragment,
              fragmentMind,
              monster.positionX,
              monster.positionY,
              monster.radius,
              -1
            )
            fragment.activity = new AsteroidActivity(
              asteroidActivity.rotationRadiansPerMilli * Math.random(),
              fragmentAngle,
              asteroidActivity.pixelsPerMilli * Math.random(),
              1,
              parts,
              null
            )
            // want the same rotation
            level.particles += fragment;
          }
          index = next
        }

    }
    explosionSoundEffect.play()
    return super.collision(monster, collidedWith, level);
  }
}
