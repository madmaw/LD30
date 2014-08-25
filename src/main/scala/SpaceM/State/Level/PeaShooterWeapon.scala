package SpaceM.State.Level

/**
 * Created by chris on 24/08/14.
 */
class PeaShooterWeapon(
  millisBetweenBullets: Long
) extends Weapon {

  var timeToFire = millisBetweenBullets

  var rapidTime: Long = 0

  var numberOfBullets: Int = 1
  var extraBulletTime: Long = 0

  var spreadAngle = Math.PI/5;

  def boost(): Unit = {
    rapidTime += 30000
  }

  def spread(): Unit = {
    numberOfBullets += 1
    extraBulletTime += 25000
  }

  override def fire(delta: Long, positionX: Int, positionY: Int, angle: Double): Array[Monster] = {

    rapidTime = Math.max(0, rapidTime - delta)
    extraBulletTime = extraBulletTime - delta
    if( extraBulletTime < 0 ) {
      if( numberOfBullets > 1 ) {
        numberOfBullets -= 1
        extraBulletTime += 15000
      } else {
        extraBulletTime = 0
      }
    }
    timeToFire -= delta
    if( timeToFire < 0 ) {
      if( rapidTime > 0 ) {
        timeToFire += millisBetweenBullets / 2
      } else {
        timeToFire += millisBetweenBullets
      }
      val mind = new ProjectileMind()
      val result = new Array[Monster](numberOfBullets)
      val partAngle = spreadAngle / numberOfBullets
      for( i <- 0 to numberOfBullets - 1 ) {
        val bulletAngle = angle - spreadAngle / 2 + i * partAngle + partAngle / 2
        val activity = new ProjectileActivity(
          0,
          bulletAngle,
          0.5,
          1
        )
        val bullet = new Monster(
          MonsterType.Bullet,
          mind,
          positionX,
          positionY,
          5,
          -1
        )
        bullet.activity = activity;
        result(i) = bullet
      }
      return result;
    } else {
      return null;
    }

  }
}
