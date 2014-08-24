package SpaceM.State.Level

/**
 * Created by chris on 24/08/14.
 */
class PeaShooterWeapon(
  millisBetweenBullets: Long
) extends Weapon {

  var timeToFire = millisBetweenBullets

  override def fire(delta: Long, positionX: Int, positionY: Int, angle: Double): Array[Monster] = {

    timeToFire -= delta
    if( timeToFire < 0 ) {
      timeToFire += millisBetweenBullets
      val mind = new ProjectileMind()
      val activity = new ProjectileActivity(
        0,
        angle,
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
      return Array(bullet)
    } else {
      return null;
    }

  }
}
