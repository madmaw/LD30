package SpaceM.State.Level

import scala.collection.mutable.ArrayBuffer

/**
 * Created by chris on 23/08/14.
 */
trait MonsterSpawner {

  def reset();

  def accumulate(mills: Long, level: LevelState, width: Int, height: Int): ArrayBuffer[Monster];
}
