import java.awt.event.{KeyEvent, KeyListener}
import java.awt.geom.Point2D
import java.awt.{Point, Color, Toolkit}
import java.io.{FileOutputStream, File}
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.{JPanel, WindowConstants, JFrame}

import SpaceM.Driver.Keyboard.KeyboardDriver
import SpaceM.Driver.OpenCV.OpenCVDriver
import SpaceM.State.Calibration.CalibrationState
import SpaceM.State.Fade.FadeOutStateProxy
import SpaceM.State.Level._
import SpaceM.State.Menu.MenuState
import org.opencv.objdetect.CascadeClassifier

/**
 * Created by chris on 23/08/14.
 */
object Main {

  def toFile(path:String): File = {
    val file = new File(path);
    if( !file.exists() ) {
      file.getParentFile.mkdirs()
      val ins = getClass().getResourceAsStream(path)
      if( ins == null ) {
        throw new RuntimeException("missing file "+path+" (are you on a supported OS?)");
      }
      val outs = new FileOutputStream(file)
      val b = new Array[Byte](1000)
      var c = 0;
      while( c >= 0 ) {
        if( c > 0 ) {
          outs.write(b, 0, c)
        }
        c = ins.read(b)
      }
      ins.close
      outs.close
    }
    return file
  }

  def main(args: Array[String]) = {

    val os = System.getProperty("os.name").toLowerCase;
    val arch = System.getProperty("os.arch").toLowerCase;
    val bit32 = "i386".equalsIgnoreCase(arch) || "i686".equalsIgnoreCase(arch)
    var libraryPath: String = null;
    if( os.startsWith("windows") ) {
      libraryPath = "opencv/win/opencv_java249.dll"
    } else if( os.startsWith("linux") ) {
      if( bit32 ) {
        libraryPath = "opencv/linux/x86_32/libopencv_java249.so"
      } else {
        libraryPath = "opencv/linux/x86_64/libopencv_java249.so"
      }
    } else {
      if( bit32 ) {
        libraryPath = "opencv/osx/x86_32/libopencv_java249.dylib"
      } else {
        libraryPath = "opencv/osx/x86_64/libopencv_java249.dylib"
      }
    }

    System.load(toFile(libraryPath).getAbsolutePath())
    val manualFaceImage =  ImageIO.read(getClass.getResource("face.png"))
    val bodyImage = ImageIO.read(getClass.getResource("body.png"))
    val armLeftImage = ImageIO.read(getClass.getResource("armleft.png"))
    val armRightImage = ImageIO.read(getClass.getResource("armright.png"))
    val legsImage = ImageIO.read(getClass.getResource("legs.png"))
    val gunImage = ImageIO.read(getClass.getResource("peashooter.png"))
    val powerUpOxygenImage = ImageIO.read(getClass.getResource("oxygen.png"))
    val thrustImage = ImageIO.read(getClass.getResource("thrust.png"))
    val bulletImage1 = ImageIO.read(getClass.getResource("bullet.png"))
    val bulletImage2 = ImageIO.read(getClass.getResource("bullet2.png"))
    val bulletImage3 = ImageIO.read(getClass.getResource("bullet3.png"))

    val faceDetector = new CascadeClassifier(toFile("opencv/haarcascade_frontalface_default.xml").getAbsolutePath)
    val leftEyeDetector = new CascadeClassifier(toFile("opencv/haarcascade_mcs_lefteye.xml").getAbsolutePath)
    val rightEyeDetector = new CascadeClassifier(toFile("opencv/haarcascade_mcs_righteye.xml").getAbsolutePath)
    val noseDetector = new CascadeClassifier(toFile("opencv/haarcascade_mcs_nose.xml").getAbsolutePath)

    val frame = new JFrame("SpaceM")
    frame.setUndecorated(true)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setResizable(false)

    val view = new JPanel()
    frame.setContentPane(view)
    view.setBackground(Color.BLACK)

    // set the frame to be the same size as the screen

    val screenSize = Toolkit.getDefaultToolkit().getScreenSize()
    frame.setBounds(0, 0, screenSize.width, screenSize.height)



    val manualDriver = new KeyboardDriver(view, manualFaceImage, 10, screenSize);
    manualDriver.start()

    val openCVDriver = new OpenCVDriver(
      faceDetector,
      leftEyeDetector,
      rightEyeDetector,
      noseDetector,
      screenSize
    )
    openCVDriver.start();

    val playerHeadRenderRadius = 40;
    val playerHeadRadius = bodyImage.getWidth()/4
    val playerRenderer = new PlayerRenderer(
      bodyImage,
      new Point(bodyImage.getWidth()/2, (bodyImage.getHeight()/3.5).toInt),
      playerHeadRenderRadius,
      armLeftImage,
      new Point(armLeftImage.getWidth()/5, ((armLeftImage.getHeight())/4).toInt),
      new Point((bodyImage.getWidth()/4).toInt, (bodyImage.getHeight()/3.4).toInt),
      armRightImage,
      new Point(armRightImage.getWidth()/5, ((armRightImage.getHeight()*3)/4).toInt),
      new Point(((bodyImage.getWidth()*3)/4).toInt, (bodyImage.getHeight()/3.4).toInt),
      legsImage,
      new Point(legsImage.getWidth/2, legsImage.getHeight/10),
      new Point((bodyImage.getWidth()/2).toInt, (bodyImage.getHeight()/1.4).toInt),
      gunImage,
      new Point((gunImage.getWidth * 0.9).toInt, (gunImage.getHeight * 0.7).toInt),
      new Point(bodyImage.getWidth()/2, (bodyImage.getHeight()/3.3).toInt)
    )
    val oxygenRenderer = new PowerUpRenderer(
      powerUpOxygenImage,
      new Point(powerUpOxygenImage.getWidth / 2, powerUpOxygenImage.getHeight/2)
    )
    val bulletRenderer = new BulletRenderer(
      Array(bulletImage1, bulletImage2, bulletImage3),
      new Point(bulletImage1.getWidth/2, bulletImage1.getHeight/2),
      80
    )
    val thrustRenderer = new ThrustRenderer(
      thrustImage,
      new Point(thrustImage.getWidth/2, thrustImage.getHeight/2)
    )
    val circleRenderer = new CircleRenderer(Color.RED)
    val spaceLevelState = new LevelState(
      openCVDriver,
      new HardCodedSpaceMonsterSpawner(Math.max(powerUpOxygenImage.getWidth(), powerUpOxygenImage.getHeight())/2),
      new HardCodedRendererFactory(
        playerRenderer,
        oxygenRenderer,
        bulletRenderer,
        thrustRenderer,
        circleRenderer
      ),
      Color.BLACK,
      playerHeadRadius,
      100D,
      0.001,
      0.01
    )

    val menuState = new MenuState(
      openCVDriver,
      new FadeOutStateProxy(spaceLevelState, 4000),
      playerHeadRenderRadius,
      playerHeadRadius * 2,
      3000,
      bodyImage,
      new Point(bodyImage.getWidth()/2, (bodyImage.getHeight()/3.3).toInt)
    )

    spaceLevelState.gameOverState = new FadeOutStateProxy(
      menuState,
      1000
    )

    /*
    val calibrateCenterStateAgain = new CalibrationState(
      manualDriver,
      openCVDriver,
      new Point2D.Double(screenSize.width/2, screenSize.height/2),
      true,
      calibrationRadius,
      spaceLevelState,
      3
    )

    val calibrateTopRightState = new CalibrationState(
      manualDriver,
      openCVDriver,
      new Point2D.Double(screenSize.width - calibrationRadius, calibrationRadius),
      false,
      calibrationRadius,
      calibrateCenterStateAgain,
      1
    )
    val calibrateTopLeftState = new CalibrationState(
      manualDriver,
      openCVDriver,
      new Point2D.Double(calibrationRadius, calibrationRadius),
      false,
      calibrationRadius,
      calibrateTopRightState,
      1
    )

    val calibrateBottomLeftState = new CalibrationState(
      manualDriver,
      openCVDriver,
      new Point2D.Double(calibrationRadius, screenSize.height - calibrationRadius),
      false,
      calibrationRadius,
      calibrateTopLeftState,
      1
    )

    val calibrateBottomRightState = new CalibrationState(
      manualDriver,
      openCVDriver,
      new Point2D.Double(screenSize.width - calibrationRadius, screenSize.height - calibrationRadius),
      false,
      calibrationRadius,
      calibrateBottomLeftState,
      1
    )

    val calibrateCenterState = new CalibrationState(
      manualDriver,
      openCVDriver,
      new Point2D.Double(screenSize.width/2, screenSize.height/2),
      true,
      calibrationRadius,
      calibrateBottomRightState,
      2
    )
    */

    val engine = new SpaceM.Engine(
      //calibrateCenterState,
      menuState,
      view
    )

    frame.addKeyListener(new KeyListener {
      override def keyTyped(k: KeyEvent): Unit = {
      }

      override def keyPressed(k: KeyEvent): Unit = {
        if( k.getKeyCode == KeyEvent.VK_ESCAPE ) {
          frame.setVisible(false)
          // TODO make synchronous
          engine.stop()
          manualDriver.stop()
          openCVDriver.stop()
          frame.dispose()
        }
      }

      override def keyReleased(p1: KeyEvent): Unit = {

      }
    })

    frame.setVisible(true)
    engine.start()


  }

}
