package SpaceM.Driver

import java.awt.Image

/**
 * Created by chris on 23/08/14.
 */
class Input(
  var positionX: Int,
  var positionY: Int,
  var targetAvailable: Boolean,
  var targetX: Int,
  var targetY: Int,
  var targetOff: Boolean,
  var personalizationImage: Image,
  var personalizationImageCX: Int,
  var personalizationImageCY: Int,
  var personalizationImageAngle: Double,
  var personalizationLeftEyeAvailable: Boolean,
  var personalizationLeftEyeX: Int,
  var personalizationLeftEyeY: Int,
  var personalizationRightEyeAvailable: Boolean,
  var personalizationRightEyeX: Int,
  var personalizationRightEyeY: Int
) {

  def copy(): Input = {
    return new Input(
      this.positionX,
      this.positionY,
      this.targetAvailable,
      this.targetX,
      this.targetY,
      this.targetOff,
      this.personalizationImage,
      this.personalizationImageCX,
      this.personalizationImageCY,
      this.personalizationImageAngle,
      this.personalizationLeftEyeAvailable,
      this.personalizationLeftEyeX,
      this.personalizationLeftEyeY,
      this.personalizationRightEyeAvailable,
      this.personalizationRightEyeX,
      this.personalizationRightEyeY
    );
  }

}
