package SpaceM.State.Level

/**
 * Created by chris on 24/08/14.
 */
trait Weapon {

  def fire(delta: Long, positionX: Int, positionY: Int, angle: Double): Array[Monster];

}
