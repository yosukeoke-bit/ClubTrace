package jp.clubtrace.mvp

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.video.Video
import kotlin.math.abs

/**
 * Tracks a small cluster of features around the club head.
 *
 * Important design choice:
 * We do NOT trust a single tracked point. We track multiple corners in an ROI
 * and use the median displacement. This is more robust against blur/reflections.
 */
class OpticalFlowClubTracker(
    private val roiRadiusPx: Int = 55
) {
    private var previousGray: Mat? = null
    private var previousFeatures: MatOfPoint2f? = null
    private var currentCenter: Point? = null

    fun initialize(frameRgba: Mat, initialCenter: Point): Boolean {
        val gray = Mat()
        Imgproc.cvtColor(frameRgba, gray, Imgproc.COLOR_RGBA2GRAY)

        val x0 = (initialCenter.x - roiRadiusPx).toInt().coerceAtLeast(0)
        val y0 = (initialCenter.y - roiRadiusPx).toInt().coerceAtLeast(0)
        val x1 = (initialCenter.x + roiRadiusPx).toInt().coerceAtMost(gray.cols() - 1)
        val y1 = (initialCenter.y + roiRadiusPx).toInt().coerceAtMost(gray.rows() - 1)

        if (x1 <= x0 || y1 <= y0) return false

        val mask = Mat.zeros(gray.size(), CvType.CV_8UC1)
        Imgproc.rectangle(mask, Point(x0.toDouble(), y0.toDouble()),
            Point(x1.toDouble(), y1.toDouble()), Scalar(255.0), -1)

        val corners = MatOfPoint()
        Imgproc.goodFeaturesToTrack(
            gray, corners, 40, 0.01, 5.0, mask,
            3, false, 0.04
        )

        if (corners.rows() < 4) return false

        previousGray?.release()
        previousFeatures?.release()
        previousGray = gray
        previousFeatures = MatOfPoint2f(*corners.toArray())
        currentCenter = initialCenter
        mask.release()
        corners.release()
        return true
    }

    fun track(nextFrameRgba: Mat): Point? {
        val prevGray = previousGray ?: return null
        val prevPts = previousFeatures ?: return null
        val oldCenter = currentCenter ?: return null

        val nextGray = Mat()
        Imgproc.cvtColor(nextFrameRgba, nextGray, Imgproc.COLOR_RGBA2GRAY)

        val nextPts = MatOfPoint2f()
        val status = MatOfByte()
        val err = MatOfFloat()

        Video.calcOpticalFlowPyrLK(
            prevGray, nextGray, prevPts, nextPts, status, err,
            Size(21.0, 21.0), 3,
            TermCriteria(TermCriteria.COUNT or TermCriteria.EPS, 30, 0.01),
            0, 1e-4
        )

        val a = prevPts.toArray()
        val b = nextPts.toArray()
        val s = status.toArray()

        val dx = mutableListOf<Double>()
        val dy = mutableListOf<Double>()
        val goodNext = mutableListOf<Point>()

        for (i in s.indices) {
            if (s[i].toInt() != 1) continue
            val ddx = b[i].x - a[i].x
            val ddy = b[i].y - a[i].y
            if (abs(ddx) > 180 || abs(ddy) > 180) continue
            dx += ddx
            dy += ddy
            goodNext += b[i]
        }

        if (dx.size < 4) {
            nextGray.release()
            nextPts.release()
            status.release()
            err.release()
            return null
        }

        fun median(v: List<Double>): Double {
            val z = v.sorted()
            return if (z.size % 2 == 1) z[z.size / 2]
            else (z[z.size / 2 - 1] + z[z.size / 2]) / 2.0
        }

        val center = Point(oldCenter.x + median(dx), oldCenter.y + median(dy))

        previousGray?.release()
        previousFeatures?.release()
        previousGray = nextGray
        previousFeatures = MatOfPoint2f(*goodNext.toTypedArray())
        currentCenter = center

        nextPts.release()
        status.release()
        err.release()
        return center
    }

    fun reset() {
        previousGray?.release()
        previousFeatures?.release()
        previousGray = null
        previousFeatures = null
        currentCenter = null
    }
}
