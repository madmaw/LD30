package SpaceM.Driver

/**
 * Created by chris on 23/08/14.
 */
trait Driver {

  def start()

  def stop()

  def calculateInputs(delta: Long): Map[Int, Input];

  def hintTargetPosition(x: Int, y: Int, center: Boolean);

}
