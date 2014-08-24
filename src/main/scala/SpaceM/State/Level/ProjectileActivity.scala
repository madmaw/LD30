package SpaceM.State.Level

/**
 * Created by chris on 23/08/14.
 */
class ProjectileActivity(
  val rotationRadiansPerMilli: Double,
  val angle: Double,
  val pixelsPerMilli: Double,
  var toughness: Int
) extends Activity {

  val sin = Math.sin(angle);
  val cos = Math.cos(angle);

}
