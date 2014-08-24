package SpaceM.State.Level

/**
 * Created by chris on 24/08/14.
 */
class FragmentMind extends Mind {

  val maxAge = 1000

  // only called when an activity has ended or
  override def think(monster: Monster, delta: Long, level: LevelState, width: Int, height: Int): Boolean = {
    monster.activityAge += delta
    monster.activity match {
      case projectile: ProjectileActivity =>
        monster.move(projectile.pixelsPerMilli * delta * projectile.cos, projectile.pixelsPerMilli * delta * projectile.sin)
    }
    return (monster.activityAge > maxAge );
  }

  override def collision(monster: Monster, collidedWith: Monster, level: LevelState): Boolean = {
    return true;
  }
}
