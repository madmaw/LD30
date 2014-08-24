package SpaceM.State.Menu

import java.awt.geom.Ellipse2D
import java.awt.{Image, Point, Color, Graphics2D}

import SpaceM.Driver.{Input, Driver}
import SpaceM.State.Level.LevelState
import SpaceM.State.State

/**
 * Created by chris on 24/08/14.
 */
class MenuState(
  driver: Driver,
  playState: State,
  headRadius: Int,
  centerRadius: Int,
  confirmTimeMillis: Long,
  helmetImage: Image,
  helmetCenter: Point
) extends State {


  var inputs: Map[Int, Input] = null;

  var centerAge: Long = 0

  override def init(): Unit = {
    centerAge = 0
  }

  override def update(age: Long, delta: Long, width: Int, height: Int): State = {
    inputs = driver.calculateInputs(delta)
    // is there a head in the center?
    val cx = width/2
    val cy = (height*2)/3
    var isin = false;
    for( (id, input) <- inputs ) {
      val dx = cx - input.positionX
      val dy = cy - input.positionY
      val rsq = dx * dx + dy * dy
      val cr = centerRadius - headRadius
      if( rsq < cr * cr ) {
        isin = true
      }
    }
    if( isin ) {
      centerAge += delta
    } else {
      centerAge -= delta
      if( centerAge < 0 ) {
        centerAge = 0;
      }
    }
    if( centerAge > this.confirmTimeMillis ) {
      return this.playState
    } else {
      return this
    }
  }

  override def render(graphics: Graphics2D, width: Int, height: Int): Unit = {
    var background = Color.BLACK
    var messages = Array("Put your helmet on!")
    if( this.centerAge > 0 ) {
      val red = Math.max(0, Math.min(255, (centerAge * 255)/this.confirmTimeMillis)).toInt
      background = new Color(red, 0, 0)
      messages = Array("Get ready for battle soldier!")
    }
    graphics.setBackground(background)
    graphics.clearRect(0, 0, width, height)

    graphics.setColor(Color.WHITE)
    var y = height.toInt/3;
    for( message <- messages ) {
      val bounds = graphics.getFontMetrics.getStringBounds(message, graphics)
      graphics.drawString(message, (width - bounds.getWidth).toInt/2, y)
      y += bounds.getHeight.toInt;
    }



    val cx = width / 2
    val cy = (height*2) / 3

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

          val scaledImageX = input.positionX - (input.personalizationImageCX * s).toInt
          val scaledImageY = input.positionY - (input.personalizationImageCY * s).toInt
          val scaledImageWidth = (imageWidth * s).toInt
          val scaledImageHeight = (imageHeight * s).toInt

          val imageGraphics = graphics.create()
          val clip = new Ellipse2D.Float(scaledImageX, scaledImageY, scaledImageWidth, scaledImageHeight)
          imageGraphics.setClip(clip)
          imageGraphics.drawImage(
            image,
            scaledImageX,
            scaledImageY,
            scaledImageWidth,
            scaledImageHeight,
            null
          )
          imageGraphics.dispose()

//          graphics.drawImage(
//            input.personalizationImage,
//            scaledImageX,
//            scaledImageY,
//            (imageWidth * s).toInt,
//            (imageHeight * s).toInt,
//            null
//          )
        }
      }
    }
    graphics.drawImage(this.helmetImage, (cx - this.helmetCenter.getX).toInt, (cy - this.helmetCenter.getY).toInt, null)

  }
}
