package jp.clubtrace.mvp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class TrajectoryOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val points = mutableListOf<PointF>()
    private val paintBack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = 7f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val paintDown = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        strokeWidth = 7f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val headPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    var splitIndex: Int = Int.MAX_VALUE
    var onTapPoint: ((PointF) -> Unit)? = null
    var tapEnabled = false

    fun setTrajectory(newPoints: List<PointF>) {
        points.clear()
        points.addAll(newPoints)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < 2) return

        fun pathFor(from: Int, to: Int): Path {
            val p = Path()
            p.moveTo(points[from].x, points[from].y)
            for (i in from + 1..to) p.lineTo(points[i].x, points[i].y)
            return p
        }

        val split = splitIndex.coerceIn(1, points.lastIndex)
        canvas.drawPath(pathFor(0, split), paintBack)
        if (split < points.lastIndex) {
            canvas.drawPath(pathFor(split, points.lastIndex), paintDown)
        }

        val h = points.last()
        canvas.drawCircle(h.x, h.y, 9f, headPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!tapEnabled) return false
        if (event.action == MotionEvent.ACTION_UP) {
            onTapPoint?.invoke(PointF(event.x, event.y))
            return true
        }
        return true
    }
}
