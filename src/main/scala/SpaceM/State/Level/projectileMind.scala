package SpaceM.State.Level

/**
 * Created by chris on 23/08/14.
 */
class ProjectileMind(

) extends Mind {
  // only called when an activity has ended or
  override def think(monster: Monster, delta: Long, level: LevelState, width: Int, height: Int): Boolean = {
    monster.activityAge += delta
    val activity = monster.activity;
    activity match {
      case asteroidActivity: ProjectileActivity =>
        val dx = asteroidActivity.cos * asteroidActivity.pixelsPerMilli * delta
        val dy = asteroidActivity.sin * asteroidActivity.pixelsPerMilli * delta
        monster.move(dx, dy)
        var result = false;
        if( dx > 0 ) {
          if( monster.positionX > width + monster.radius ) {
            result = true;
          }
        } else if( dx < 0 ) {
          if( monster.positionX < -monster.radius ) {
            result = true;
          }
        }
        if( dy > 0 ) {
          if( monster.positionY > height + monster.radius ) {
            result = true;
          }
        } else if( dy < 0 ) {
          if( monster.positionY < -monster.radius ) {
            result = true;
          }
        }
        return result
      case _ =>
        return true;
    }
  }

  override def collision(monster: Monster, collidedWith: Monster, level: LevelState): Boolean = {
    if( monster.monsterType == MonsterType.Jelly ) {
      monster.activity match {
        case projectileActivity: ProjectileActivity =>
          projectileActivity.toughness -= 1
          if( projectileActivity.toughness > 0 ) {
            monster.radius = (monster.radius * projectileActivity.toughness) / (projectileActivity.toughness+1)
            return false
          } else {
            return true;
          }
      }
    } else {
      return true;
    }
  }
}
