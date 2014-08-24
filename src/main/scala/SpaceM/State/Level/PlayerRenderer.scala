package SpaceM.State.Level

import java.awt.geom.Ellipse2D
import java.awt.{Point, Image, Color, Graphics2D}

/**
 * Created by chris on 23/08/14.
 */
class PlayerRenderer(
  val bodyImage: Image,
  val headCenter: Point,
  val headRadius: Int,
  val leftArmImage: Image,
  val leftArmShoulder: Point,
  val leftShoulder: Point,
  val rightArmImage: Image,
  val rightArmShoulder: Point,
  val rightShoulder: Point,
  val legsImage: Image,
  val legsImageJoint: Point,
  val legsJoint: Point,
  val gunImage: Image,
  val gunImageJoint: Point,
  val gunJoint: Point
) extends MonsterRenderer {

  override def render(graphics: Graphics2D, monster: Monster): Unit = {

    val mind = monster.mind;
    mind match {
      case playerMind: PlayerMind =>
        // draw the image
        val image = playerMind.input.personalizationImage
        val imageWidth = image.getWidth(null);
        val imageHeight = image.getHeight(null);
        val sx = (headRadius * 2).toDouble / imageWidth;
        val sy = (headRadius * 2).toDouble / imageHeight;
        var s = sx;
        if( sx > sy ) {
          s = sy;
        }

        val bodyImageX = monster.positionX - this.headCenter.x
        val bodyImageY = monster.positionY - this.headCenter.y

        val copy = graphics.create()
        copy match {
          case copy2D: Graphics2D =>
            val gx = bodyImageX + gunJoint.x
            val gy = bodyImageY + gunJoint.y
            copy2D.rotate(playerMind.input.personalizationImageAngle, gx, gy)
            copy2D.drawImage(gunImage, gx - gunImageJoint.x, gy - gunImageJoint.y, null)
        }
        copy.dispose()



        val leftShoulderGraphics = graphics.create();
        leftShoulderGraphics match {
          case lsg: Graphics2D =>
            val lsx = bodyImageX + leftShoulder.x
            val lsy = bodyImageY + leftShoulder.y
            lsg.rotate(playerMind.input.personalizationImageAngle/2 + Math.PI - Math.PI/3, lsx, lsy)
            lsg.drawImage(leftArmImage, lsx - leftArmShoulder.x, lsy - leftArmShoulder.y, null)
        }
        leftShoulderGraphics.dispose()

        val rightShoulderGraphics = graphics.create()
        rightShoulderGraphics match {
          case rsg: Graphics2D =>
            val rsx = bodyImageX + rightShoulder.x
            val rsy = bodyImageY + rightShoulder.y
            rsg.rotate(playerMind.input.personalizationImageAngle/2 + Math.PI/3, rsx, rsy)
            rsg.drawImage(rightArmImage, rsx - rightArmShoulder.x, rsy - rightArmShoulder.y, null)
        }

        val legsGraphics = graphics.create()
        legsGraphics match {
          case lg: Graphics2D =>
            val lx = bodyImageX + legsJoint.x
            val ly = bodyImageY + legsJoint.y
            lg.rotate(playerMind.input.personalizationImageAngle/2, lx, ly)
            lg.drawImage(legsImage, lx - legsImageJoint.x, ly - legsImageJoint.y, null)
        }

        val scaledImageX = monster.positionX - (playerMind.input.personalizationImageCX * s).toInt
        val scaledImageY = monster.positionY - (playerMind.input.personalizationImageCY * s).toInt
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

        if( playerMind.input.targetAvailable && !playerMind.input.targetOff ) {
          graphics.drawLine(
            monster.positionX,
            monster.positionY,
            playerMind.input.targetX,
            playerMind.input.targetY
          )
        }

        graphics.drawImage(
          this.bodyImage,
          bodyImageX,
          bodyImageY,
          null
        )


    }


    graphics.setColor(Color.WHITE)
    graphics.drawOval(
      monster.positionX - monster.radius,
      monster.positionY - monster.radius,
      monster.radius * 2,
      monster.radius * 2
    )

  }

}
