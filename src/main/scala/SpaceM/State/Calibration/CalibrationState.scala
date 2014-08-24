package SpaceM.State.Calibration

import java.awt.geom.Point2D
import java.awt.{Color, Graphics2D}

import SpaceM.Driver.{Driver, Input}
import SpaceM.State.State

/**
 * Created by chris on 23/08/14.
 */
class CalibrationState(
  val manualDriver: Driver,
  val calibratedDriver: Driver,
  val position: Point2D,
  val center: Boolean,
  val radius: Int,
  val nextState: State,
  val minTimeSeconds: Long
) extends State {

  var calibrationOK = false;
  var inputs: Map[Int, Input] = null;
  var success = false;
  var requiredTimeMillis = 0L;

  val headRadius = 100;

  override def init() = {
    this.update(0, 0, 0, 0);
    success = false;
    requiredTimeMillis = minTimeSeconds.toLong * 1000L;
  }


  override def update(age: Long, delta: Long, width: Int, height: Int): State = {
    var result: State = this;
    val manualInputs = manualDriver.calculateInputs(delta);
    val calibratedInputs = calibratedDriver.calculateInputs(delta)
    val numberOfCalibratedInputs = calibratedInputs.size
    inputs = calibratedInputs
    if( numberOfCalibratedInputs > 0 ) {
      calibrationOK = true;
      for((inputId, input) <- calibratedInputs) {
        // is the eye available?
        if( !input.personalizationLeftEyeAvailable && !input.personalizationRightEyeAvailable ) {
          calibrationOK = false;
        }
      }
    } else {
      calibrationOK = false;
    }
    if( calibrationOK ) {
      var allOff = true;
      for( (inputId, input) <- manualInputs ) {
        if( input.targetOff ) {
          allOff = false;
          // is it in the area and clicked?
          val dx = input.targetX - this.position.getX
          val dy = input.targetY - this.position.getY
          val dsq = dx * dx + dy * dy
          if( dsq < radius * radius ) {
            calibratedDriver.hintTargetPosition(input.targetX, input.targetY, center)
            requiredTimeMillis -= delta;
          }
        }
      }
      if( allOff && requiredTimeMillis < 0 ) {
        result = this.nextState;
      }
    }
    return result;
  }

  override def render(graphics: Graphics2D, width: Int, height: Int): Unit = {
    graphics.setBackground(Color.BLACK)
    graphics.clearRect(0, 0, width, height)

    var messages: Array[String] = null;

    // render the faces....
    if( inputs != null ) {
      for( (id, input) <- inputs ) {
        val image = input.personalizationImage;
        if( image != null ) {
          // aspect ratio
          // adjust cx, cy for scaling
          val imageWidth = input.personalizationImage.getWidth(null);
          val imageHeight = input.personalizationImage.getHeight(null);
          val sx = (headRadius * 2).toDouble / imageWidth;
          val sy = (headRadius * 2).toDouble / imageHeight;
          var s = sx;
          if( sx > sy ) {
            s = sy;
          }
          graphics.drawImage(
            input.personalizationImage,
            input.positionX - (input.personalizationImageCX * s).toInt,
            input.positionY - (input.personalizationImageCY * s).toInt,
            (imageWidth * s).toInt,
            (imageHeight * s).toInt,
            null
          )
        }
      }
    }

    if( calibrationOK ) {
      graphics.setColor(Color.GREEN)
      graphics.fillOval(this.position.getX.toInt - this.radius, this.position.getY.toInt - this.radius, this.radius * 2, this.radius * 2)

      graphics.setColor(Color.BLACK)
      graphics.drawString(Math.ceil(requiredTimeMillis/1000D).toInt.toString, this.position.getX.toInt, this.position.getY.toInt)

      messages = Array("Center your head(s) in the circle(s)", "Look at and long-click on the green circle (longer the better)")

      if( inputs != null ) {
        graphics.setColor(Color.YELLOW);
        val inputCount = inputs.size
        for( inputIndex <- 0 to inputCount-1 ) {
          val x = width / 2 - (inputCount * headRadius) + (inputIndex*2) * headRadius;
          val y = (height * 2) / 3 - headRadius;
          graphics.drawOval(x + 10, y + 10, (headRadius - 10) * 2, (headRadius - 10) * 2)
        }
      }

    } else {
      messages = Array("Ensure your web camera can see you and you are in good light")
    }

    graphics.setColor(Color.WHITE)
    var y = height.toInt/3;
    for( message <- messages ) {
      val bounds = graphics.getFontMetrics.getStringBounds(message, graphics)
      graphics.drawString(message, (width - bounds.getWidth).toInt/2, y)
      y += bounds.getHeight.toInt;
    }


  }
}
