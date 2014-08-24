package SpaceM.State.Level

import SpaceM.State.Level.MonsterType.MonsterType

/**
 * Created by chris on 23/08/14.
 */
class Monster(
  val monsterType: MonsterType,
  val mind: Mind,
  var positionX: Int,
  var positionY: Int,
  var radius: Int,
  val playerInputId: Int
) {

  var activity: Activity = null;
  var activityAge: Long = 0;
  var unusedDX = 0D
  var unusedDY = 0D

  def setActivity(activity: Activity): Unit = {
    if( activity != this.activity ) {
      this.activity = activity
      this.activityAge = 0L
    }
  }

  def move(dx: Double, dy: Double): Unit = {
    val ddx = dx + unusedDX;
    val ddy = dy + unusedDY;

    var idx: Int = 0;
    if( ddx < 0 ) {
      idx = Math.ceil(ddx).toInt
    } else {
      idx = Math.floor(ddx).toInt
    }
    var idy: Int = 0;
    if( ddy < 0 ) {
      idy = Math.ceil(ddy).toInt
    } else {
      idy = Math.floor(ddy).toInt
    }

    positionX += idx;
    positionY += idy;

    unusedDX = ddx - idx;
    unusedDY = ddy - idy;
  }

}
