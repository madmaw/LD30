package SpaceM.State.Level

/**
 * Created by chris on 23/08/14.
 */
trait Mind {

  // only called when an activity has ended or
  def think(monster: Monster, delta: Long, level: LevelState, width: Int, height: Int): Boolean

  def collision(monster: Monster, collidedWith: Monster, level: LevelState): Boolean
}
