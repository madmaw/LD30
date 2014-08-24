package SpaceM.Driver.OpenCV

import java.awt.Dimension
import java.io.{FileOutputStream, ByteArrayInputStream}
import java.util
import javax.imageio.ImageIO

import SpaceM.Driver.{Input, Driver}
import org.opencv.core._
import org.opencv.highgui.{Highgui, VideoCapture}
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.{Objdetect, CascadeClassifier}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.runtime.RichInt
import scala.util.Sorting

/**
 * Created by chris on 23/08/14.
 */
class OpenCVDriver(
  faceDetector: CascadeClassifier,
  leftEyeDetector: CascadeClassifier,
  rightEyeDetector: CascadeClassifier,
  noseDetector: CascadeClassifier,
  dimensions: Dimension
)extends Driver {

  def WHITE = new Scalar(255, 255, 255);
  def BLACK = new Scalar(0, 0, 0);

  var running = false;

  var numberOfInputs = 0;

  val hints = new mutable.HashMap[Int, ArrayBuffer[OpenCVHint]]()

  var currentInputs: Map[Int, Input] = Map[Int, Input]();
  var inputHolders = new mutable.HashMap[Int, OpenCVInputParams]();

  def balanceCircle(cx: Int, cy: Int, r: Int, mat: Mat, tmp: Mat): Double = {
    val count = Math.PI * r * r;
    Core.rectangle(tmp, new Point(0, 0), new Point(tmp.width(), tmp.height()), BLACK, -1);
    Core.ellipse(tmp, new Point(cx, cy), new Size(r, r), 0, -90, 90, WHITE, -1);
    Core.bitwise_and(mat, tmp, tmp);
    val left = Core.sumElems(tmp);

    Core.rectangle(tmp, new Point(0, 0), new Point(tmp.width(), tmp.height()), BLACK, -1);
    Core.ellipse(tmp, new Point(cx, cy), new Size(r, r), 0, 90, -90, WHITE, -1);
    Core.bitwise_and(mat, tmp, tmp);
    val right = Core.sumElems(tmp);

    return 2 * (255 - Math.abs(left.`val`(0) - right.`val`(0)))/(255D * count);
  }

  def meanCircle(cx: Int, cy: Int, minRadius: Int, maxRadius: Int, mat: Mat, tmp: Mat, minProportion: Double, maxProportion: Double): Double = {
    val proportion = maxProportion - minProportion;
    Core.rectangle(tmp, new Point(0, 0), new Point(tmp.width(), tmp.height()), BLACK, -1);
    val c = new Point(cx, cy);
    Core.circle(tmp, c, maxRadius, WHITE, -1);
    var count = Math.PI * maxRadius * maxRadius;
    if( minRadius > 0 ) {
      Core.circle(tmp, c, minRadius, BLACK, -1);
      count -= Math.PI * minRadius * minRadius;
    }
    Core.rectangle(tmp, new Point(0, cy - maxRadius + maxRadius * 2 * (1 - minProportion)), new Point(tmp.width(), cy - maxRadius + maxRadius * 2 * (1 - maxProportion)), BLACK, -1);
    count *= proportion;
    Core.bitwise_and(mat, tmp, tmp);
    val total = Core.sumElems(tmp);
    return total.`val`(0) / count;
  }

  def analyseIris(fullGray:Mat, eyeArea:Rect, midMult:Double, edgeMult:Double, left:Boolean): Rect = {
    val lowerEyeArea = new Rect(eyeArea.x, eyeArea.y+(eyeArea.height*2)/8, eyeArea.width, (eyeArea.height*6)/8);
    val gray = fullGray.submat(lowerEyeArea)
    val inv = gray;
    Core.bitwise_not(gray, inv);
    var blockSize = Math.max(1, eyeArea.width / 5);
    if( blockSize % 2 == 0 ) {
      blockSize = blockSize + 1;
    }
    Imgproc.adaptiveThreshold(inv, inv, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize, 1);
    /*
    //Imgproc.blur(inv, inv, new Size(3, 3))
    Imgproc.Canny(inv, inv, 100, 255)

    val circles = new Mat();
    Imgproc.HoughCircles(
      inv,
      circles,
      Imgproc.CV_HOUGH_GRADIENT,
      10,
      inv.rows()/16
    );
    var col = 0;
    while( col < circles.cols ) {
      val circle = circles.get(0, col)
      Core.circle(inv, new Point(circle(0).toInt, circle(1).toInt), circle(2).toInt, WHITE)
      col += 1;
    }
    */

    val oval = Mat.zeros(gray.size(), CvType.CV_8UC1);
    var cDiv = 2.05;
    if( left ) {
      cDiv = 1.95;
    }
    val c = new Point(gray.width()/cDiv, gray.height()/2.6);
    var cAngle = 5;
    if( left ) {
      cAngle = -cAngle;
    }
    Core.ellipse(oval, c, new Size(gray.width()/4, gray.height()/3), cAngle, 0, 360, WHITE, -1);
    Core.bitwise_and(oval, inv, inv);

    var totalWeight = 0.0;
    var totalWeightedYOff = 0.0;
    val columnWeights = new Array[Double](inv.width());

    // 0 horizontal 1 = diagonal (along eye box)
    var eyeTilt = -0.2;
    if( left ) {
      eyeTilt = -eyeTilt;
    }
    val blinkMidY = gray.height()/2.7;

    var blinkCyStart = 0.0;
    var blinkCyEnd = 0.0;

    var x = inv.width()
    while( x>0 ) {
      x = x-1;
      val dx = x - c.x;
      val mult = (Math.abs(dx) * (edgeMult - midMult) * 2)/inv.width() + midMult;

      var columnWeight = 0.0;
      val blinkCy = (dx / c.x) * eyeTilt * lowerEyeArea.height/2 + blinkMidY;
      if( x == 0 ) {
        blinkCyStart = blinkCy;
      } else if( x == inv.width() - 1 ) {
        blinkCyEnd = blinkCy;
      }
      var y = inv.height();
      while( y > 0 ) {
        y = y - 1;
        // TODO reuse this array
        val values = inv.get(y, x);
        val p = values(0);
        if( p > 0 ) {
          totalWeight += p;
          val dy = y - blinkCy;
          totalWeightedYOff += (dy * p);
          columnWeight += p;
        }
      }
      columnWeights(x) = columnWeight * mult;
    }

    val blinkY = (totalWeightedYOff * 2) / totalWeight;

    // debug only
    //inv.copyTo(gray);
    val big = new Mat();
    Imgproc.resize(inv, big, new Size(inv.width() * 2, inv.height() * 2));
    Imgproc.cvtColor(big, big, Imgproc.COLOR_GRAY2RGBA);
    Core.line(big, new Point(0, blinkCyStart*2), new Point(big.width(), blinkCyEnd*2), new Scalar(255, 0, 0), 1);

    var result: Rect = null;

    if( blinkY <= 0 ) {

      val d = Math.round((gray.height()*2)/3.3f);

      var x = gray.width()/2;
      var y = gray.height()/2;
      var maxMean = 0.0;
      val r = d/2;
      val highlightR = 0;
      val minPupilR = r/4;
      val whiteR = (r * 3) / 2;
      val tmp = new Mat(inv.rows(), inv.cols(), inv.`type`());

      var cx = r;
      while( cx<gray.width()-r ) {
        cx = cx + 1
        val dx = x - c.x;
        val mult = (Math.abs(dx) * (edgeMult - midMult) * 2)/inv.width() + midMult;
        var cy = r;
        while( cy<gray.height()-r ) {
          cy = cy + 1;

          //                    ArrayList<Double> values = extractValues(cx, cy, highlightR, r, inv, true);
          //                    double mean = mean(values, null);
          val irisMean = meanCircle(cx, cy, highlightR, r, inv, tmp, 0.65, 1);
          val whiteMean = 255 - meanCircle(cx, cy, r, whiteR, inv, tmp, 0.5, 1);
          val pupilMean = 255 - meanCircle(cx, cy, 0, minPupilR, gray, tmp, 0.5, 0.75);
          var mean = (Math.PI * irisMean * r * r) + (Math.PI * pupilMean * minPupilR * minPupilR * mult) + (Math.PI * whiteMean * (whiteR * whiteR - r * r));
          if( mean > maxMean ) {
            val balance = balanceCircle(cx, cy, whiteR, inv, tmp);
            mean *= Math.sqrt(balance);
            // check the balance
            if( mean > maxMean ) {
              x = cx;
              y = cy;
              maxMean = mean;
            }
          }
        }

      }
      result = new Rect(
        lowerEyeArea.x + x - r,
        lowerEyeArea.y + y - r,
        d,
        d
      );

    }

    return result;
  }

  override def start(): Unit = {
    this.running = true;
    val videoCapture = new VideoCapture(0)
    val thread = new Thread() {
      override def run(): Unit = {
        val temp = new Mat()
        val bytes = new MatOfByte()
        val ext = ".png"
        while( running ) {
          while( !videoCapture.read(temp) ) {
            println("...waiting...")
          }
          val mat = new Mat();
          Imgproc.cvtColor(temp, mat, Imgproc.COLOR_RGB2GRAY)
          val width = mat.cols()
          val height = mat.rows()

          // do some analysis
          val faceRects = new MatOfRect()
          // base sizes off the preview size
          faceDetector.detectMultiScale(
            mat,
            faceRects,
            1.10,
            5,
            Objdetect.CASCADE_SCALE_IMAGE,
            new Size(width/7, height/5),
            new Size(width/2, height)
          )

          val faceRectsArray = faceRects.toArray;
          // attempt to correlate the faces with any existing

          val inputs = new mutable.HashMap[Int, Input]()

          var index = 0;
          while( index < faceRectsArray.length ) {
            val faceRect  = faceRectsArray(index)
            val faceMat = mat.submat(faceRect);

            val noseRect = new Rect(
              faceRect.width/3,
              faceRect.height/4,
              faceRect.width/3,
              faceRect.height/2
            )
            val noseMat = new Mat(faceMat, noseRect)
            val noseRects = new MatOfRect()
            noseDetector.detectMultiScale(
              noseMat,
              noseRects,
              1.1,
              2,
              Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE,
              new Size(20, 20),
              new Size()
            )
            // guess nose
            var nose: Rect = null;
            val noseRectArray = noseRects.toArray
            if( noseRectArray.length > 0 ) {
              nose = noseRectArray(0)
            }

            val inputParams = inputHolders.getOrElse(index, null)

            // eyes
            val leftEyeSampleRect = new Rect(
              faceRect.x,
              (faceRect.y + faceRect.height/4.5).toInt,
              faceRect.width/2,
              faceRect.height/3
            );
            val leftEyeFaceMat = new Mat(mat, leftEyeSampleRect)
            val leftEyeRects = new MatOfRect();
            leftEyeDetector.detectMultiScale(
              leftEyeFaceMat,
              leftEyeRects,
              1.15,
              2,
              Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE,
              new Size(20, 20),
              new Size()
            );
            val leftEyeRectsArray = leftEyeRects.toArray
            var leftPupil: Rect = null;
            val leftEyeFound = leftEyeRectsArray.length > 0;
            var leftEyeRect: Rect = null;
            if( leftEyeFound ) {
              leftEyeRect = leftEyeRectsArray(0);
              if( inputParams != null ) {
                leftPupil = analyseIris(leftEyeFaceMat, leftEyeRect, 1, 0.6, true);
              }
            }

            val rightEyeSampleRect = new Rect(
              faceRect.x + faceRect.width/2,
              (faceRect.y + faceRect.height/4.5).toInt,
              faceRect.width/2,
              faceRect.height/3
            );
            val rightEyeFaceMat = new Mat(mat, rightEyeSampleRect)
            val rightEyeRects = new MatOfRect();
            rightEyeDetector.detectMultiScale(
              rightEyeFaceMat,
              rightEyeRects,
              1.15,
              2,
              Objdetect.CASCADE_FIND_BIGGEST_OBJECT | Objdetect.CASCADE_SCALE_IMAGE,
              new Size(20, 20),
              new Size()
            );
            val rightEyeRectsArray = rightEyeRects.toArray
            var rightPupil: Rect = null;
            val rightEyeFound = rightEyeRectsArray.length > 0;
            var rightEyeRect: Rect = null;
            if( rightEyeFound ) {
              rightEyeRect = rightEyeRectsArray(0);
              if( inputParams != null ) {

                rightPupil = analyseIris(rightEyeFaceMat, rightEyeRect, 1, 0.6, true);
              }
            }

            // adjust so the coordinates are screen coordinates (flip and scale)
            val sx = 1 - faceRect.x.toDouble / (width - faceRect.width).toDouble
            val sy = faceRect.y.toDouble / (height - faceRect.height).toDouble;

            val cx = dimensions.width * sx;
            val cy = dimensions.height * sy;

            // do this last
            Core.flip(faceMat, faceMat, 1)

//            val mean = Core.mean(faceMat)
//            Imgproc.threshold(faceMat, faceMat, mean.`val`(0), 255, Imgproc.THRESH_BINARY)

            Highgui.imencode(ext, faceMat, bytes)
            val ins = new ByteArrayInputStream(bytes.toArray)
            val faceImage = ImageIO.read(ins)

            var faceCX = noseRect.x + noseRect.width/2;
            var faceCY = noseRect.y + noseRect.height/2;
            if( nose != null ) {
              faceCX = noseRect.x + nose.x + nose.width/2;
              faceCY = noseRect.y + nose.y + nose.height/2;
            }

            var leftPupilCX = 0;
            var leftPupilCY = 0;
            val leftPupilAvailable = leftEyeFound && leftPupil != null
            if( leftPupilAvailable ) {
              leftPupilCX = leftEyeSampleRect.x + leftPupil.x + leftPupil.width/2 - faceRect.x
              leftPupilCY = leftEyeSampleRect.y + leftPupil.y + leftPupil.height/2 - faceRect.y
            }

            var rightPupilCX = 0;
            var rightPupilCY = 0;
            val rightPupilAvailable = rightEyeFound && rightPupil != null
            if( rightPupilAvailable ) {
              rightPupilCX = rightEyeSampleRect.x + rightPupil.x + rightPupil.width/2 - faceRect.x
              rightPupilCY = rightEyeSampleRect.y + rightPupil.y + rightPupil.height/2 - faceRect.y
            }



            var targetX = 0;
            var targetY = 0;
            if( inputParams != null && nose != null ) {
              var samples = 0;
              if( leftPupil != null ) {
                val dx = leftPupilCX - faceCX
                val dy = leftPupilCY - faceCY
                val sx = dx.toDouble / faceRect.width.toDouble;
                val sy = dy.toDouble / faceRect.height.toDouble;
                val dsx = (inputParams.leftScaleCX - sx);
                val dsy = (sy - inputParams.leftScaleCY);
                targetX += (cx + dimensions.width/2 * dsx/inputParams.leftMaxRadius).toInt
                targetY += (cy + dimensions.height/2 * dsy/inputParams.leftMaxRadius).toInt
//                targetX = (cx + dimensions.width/2 * dsx / (faceRect.width * inputParams.leftMaxRadius)).toInt
//                targetY = (cy + dimensions.height/2 * dsy / (faceRect.width * inputParams.leftMaxRadius)).toInt
                samples += 1
              }
              if( rightPupil != null ) {
                val dx = rightPupilCX - faceCX
                val dy = rightPupilCY - faceCY
                val sx = dx.toDouble / faceRect.width.toDouble;
                val sy = dy.toDouble / faceRect.height.toDouble;
                val dsx = (inputParams.rightScaleCX - sx);
                val dsy = (sy - inputParams.rightScaleCY);
                targetX += (cx + dimensions.width/2 * dsx/inputParams.rightMaxRadius).toInt
                targetY += (cy + dimensions.height/2 * dsy/inputParams.rightMaxRadius).toInt
                samples += 1
              }
              if( samples > 0) {
                targetX /= samples
                targetY /= samples
              }
            }

            var faceAngle: Double = 0
            if( rightEyeRect != null && leftEyeRect != null ) {
              val x1 = rightEyeRect.x + rightEyeRect.width/2
              val y1 = rightEyeRect.y + rightEyeRect.height/2
              val x2 = leftEyeRect.x + leftEyeRect.width/2
              val y2 = leftEyeRect.y + leftEyeRect.height/2
              val dx = x2 - x1
              val dy = y2 - y1
              faceAngle = Math.atan2(dy.toDouble, dx.toDouble)
            }

            val input = new Input(
              Math.round(cx).toInt,
              Math.round(cy).toInt,
              (leftEyeFound || rightEyeFound) && nose != null && inputParams != null,
              targetX,
              targetY,
              rightPupil == null && leftPupil == null,
              faceImage,
              faceCX,
              faceCY,
              faceAngle,
              leftPupilAvailable,
              leftPupilCX,
              leftPupilCY,
              rightPupilAvailable,
              rightPupilCX,
              rightPupilCY
            )
            inputs(index) = input;
            index += 1
          }
          currentInputs = inputs.toMap;
        }
      }
    }
    thread.start();
  }

  override def stop(): Unit = {
    this.running = false;
  }

  override def calculateInputs(delta: Long): Map[Int, Input] = {
    return this.currentInputs;
  }

  override def hintTargetPosition(x: Int, y: Int, center: Boolean): Unit = {
    for( (id, input) <- this.currentInputs ) {
      var idHints = this.hints.getOrElse(id, null)
      if( idHints == null ) {
        idHints = new ArrayBuffer[OpenCVHint]()
        this.hints(id) = idHints
      }
      idHints += new OpenCVHint(
        id,
        x,
        y,
        center,
        input.copy()
      );
    }
    processHints();
  }

  def processHints() = {
    for( (id, hints) <- this.hints) {
      val centerHints = hints.filter({ hint =>
        hint.center && hint.input.personalizationLeftEyeAvailable && hint.input.personalizationRightEyeAvailable;
      })
      val otherHints = hints.filter({ hint =>
        !hint.center && hint.input.personalizationLeftEyeAvailable && hint.input.personalizationRightEyeAvailable;
      })

      if( centerHints.length > 0 && otherHints.length > 0 ) {
        val sortedLeftCenterHints = Sorting.stableSort(centerHints, { hint: OpenCVHint =>
          val dx = hint.input.personalizationLeftEyeX - hint.input.personalizationImageCX
          val dy = hint.input.personalizationLeftEyeY - hint.input.personalizationImageCY
          dx * dx + dy * dy
        })
        val sortedRightCenterHints = Sorting.stableSort(centerHints, { hint: OpenCVHint =>
          val dx = hint.input.personalizationRightEyeX - hint.input.personalizationImageCX
          val dy = hint.input.personalizationRightEyeY - hint.input.personalizationImageCY
          dx * dx + dy * dy
        })
        val leftCenter = sortedLeftCenterHints(sortedLeftCenterHints.length/2);
        val leftCDX = leftCenter.input.personalizationLeftEyeX - leftCenter.input.personalizationImageCX
        val leftCDY = leftCenter.input.personalizationLeftEyeY - leftCenter.input.personalizationImageCY
        val rightCenter = sortedRightCenterHints(sortedRightCenterHints.length/2);
        val rightCDX = rightCenter.input.personalizationRightEyeX - rightCenter.input.personalizationImageCX
        val rightCDY = rightCenter.input.personalizationRightEyeY - rightCenter.input.personalizationImageCY


        val sortedLeftHints = Sorting.stableSort(otherHints, (hint: OpenCVHint) => {
          val dx = hint.input.personalizationLeftEyeX - leftCenter.input.personalizationLeftEyeX
          val dy = hint.input.personalizationLeftEyeY - leftCenter.input.personalizationLeftEyeY
          dx * dx + dy * dy
        })
        val sortedRightHints = Sorting.stableSort(otherHints, (hint: OpenCVHint) => {
          val dx = hint.input.personalizationRightEyeX - rightCenter.input.personalizationRightEyeX
          val dy = hint.input.personalizationRightEyeY - rightCenter.input.personalizationRightEyeY
          dx * dx + dy * dy
        })
        val left = sortedLeftHints(sortedLeftHints.length/2)
        val leftDX = left.input.personalizationLeftEyeX - leftCenter.input.personalizationLeftEyeX
        val leftDY = left.input.personalizationLeftEyeY - leftCenter.input.personalizationLeftEyeY
        val leftRadius = Math.sqrt(leftDX * leftDX + leftDY * leftDY)

        val right = sortedRightHints(sortedRightHints.length/2)
        val rightDX = right.input.personalizationRightEyeX - rightCenter.input.personalizationRightEyeX
        val rightDY = right.input.personalizationRightEyeY - rightCenter.input.personalizationRightEyeY
        val rightRadius = Math.sqrt(rightDX * rightDX + rightDY * rightDY)

        val leftWidth = left.input.personalizationImage.getWidth(null).toDouble;
        val rightWidth = right.input.personalizationImage.getWidth(null).toDouble

        val inputParams = new OpenCVInputParams(
          leftCDX.toDouble/leftWidth,
          leftCDY.toDouble/left.input.personalizationImage.getHeight(null).toDouble,
          leftMaxRadius = leftRadius.toDouble / leftWidth,
          rightCDX.toDouble/rightWidth,
          rightCDY.toDouble/right.input.personalizationImage.getHeight(null).toDouble,
          rightMaxRadius = rightRadius.toDouble / rightWidth
        )
        inputHolders(id) = inputParams;
      }
    }
  }
}
