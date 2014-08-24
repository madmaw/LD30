package SpaceM.Driver.Keyboard

import java.awt.{Dimension, Image, Component}
import java.awt.event._

import SpaceM.Driver.{Input, Driver}

import scala.collection.{immutable, mutable}
import scala.collection.mutable.HashMap

/**
 * Created by chris on 23/08/14.
 */
class KeyboardDriver(
  val component: Component,
  val personalizationImage: Image,
  val playerSpeed: Int,
  val dimensions: Dimension
)  extends Driver {

  val keysDown = new HashMap[Int, Boolean];

  val keyListener = new KeyListener {

    override def keyTyped(p1: KeyEvent): Unit = {

    }

    override def keyPressed(e: KeyEvent): Unit = {
      val keyCode = e.getKeyCode
      keysDown += keyCode -> true
    }

    override def keyReleased(e: KeyEvent): Unit = {
      val keyCode = e.getKeyCode
      keysDown += keyCode -> false
    }
  }

  val mouseListener = new MouseListener {

    override def mouseExited(e: MouseEvent): Unit = {
      numberOfInputs = 0
      input.targetX = e.getX
      input.targetY = e.getY
    }

    override def mouseClicked(e: MouseEvent): Unit = {

    }

    override def mouseEntered(e: MouseEvent): Unit = {
      numberOfInputs = 1
      input.targetX = e.getX
      input.targetY = e.getY
    }

    override def mousePressed(e: MouseEvent): Unit = {
      numberOfInputs = 1
      input.targetOff = true
      input.targetX = e.getX
      input.targetY = e.getY
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      input.targetOff = false
      input.targetX = e.getX
      input.targetY = e.getY
    }
  }

  val mouseMotionListener = new MouseMotionListener {
    override def mouseDragged(e: MouseEvent): Unit = {
      input.targetOff = true
      input.targetX = e.getX
      input.targetY = e.getY
    }

    override def mouseMoved(e: MouseEvent): Unit = {
      input.targetOff = false
      input.targetX = e.getX
      input.targetY = e.getY
    }
  }

  var numberOfInputs = 0

  var input = new Input(
    (dimensions.getWidth/2).toInt,
    ((dimensions.getHeight*2)/3).toInt,
    true,
    (component.getWidth/2).toInt,
    0,
    false,
    personalizationImage,
    personalizationImage.getWidth(component)/2,
    personalizationImage.getHeight(component)/3,
    0,
    true,
    personalizationImage.getWidth(component)/3,
    personalizationImage.getHeight(component)/2,
    true,
    (personalizationImage.getWidth(component)*2)/3,
    personalizationImage.getHeight(component)/3
  )

  override def start(): Unit = {
    this.component.addKeyListener(this.keyListener)
    this.component.addMouseListener(this.mouseListener)
  }

  override def stop(): Unit = {
    this.component.removeKeyListener(this.keyListener)
    this.component.removeMouseListener(this.mouseListener)
  }

  override def calculateInputs(delta: Long): Map[Int, Input] = {
    // adjust the position on the fly
    val left = this.keysDown.getOrElse(KeyEvent.VK_LEFT, false)
    val right = this.keysDown.getOrElse(KeyEvent.VK_RIGHT, false)
    val up = this.keysDown.getOrElse(KeyEvent.VK_UP, false)
    val down = this.keysDown.getOrElse(KeyEvent.VK_DOWN, false)
    var dx = 0;
    var dy = 0;
    val v = ((this.playerSpeed * delta) / 1000).toInt;
    if( left ) {
      dx -= v;
    }
    if( right ) {
      dx += v;
    }
    if( up ) {
      dy -= v;
    }
    if( down ) {
      dy += v;
    }
    this.input.positionX += dx
    this.input.positionY += dy
    // always return the same one
    return Map(0 -> this.input)
  }

  override def hintTargetPosition(x: Int, y: Int, center: Boolean): Unit = {
    // ignore, we're perfect
  }

}
