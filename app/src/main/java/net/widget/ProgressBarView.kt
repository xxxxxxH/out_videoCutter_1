package net.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import net.basicmodel.R
import net.interfaces.OnProgressVideoListener
import net.interfaces.OnRangeSeekBarListener

class ProgressBarView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr), OnRangeSeekBarListener, OnProgressVideoListener {
    private var mProgressHeight = 0
    private var mViewWidth = 0
    private val mBackgroundColor = Paint()
    private val mProgressColor = Paint()
    private var mBackgroundRect: Rect? = null
    private var mProgressRect: Rect? = null
    private fun init() {
        val lineProgress = ContextCompat.getColor(context, R.color.line_color)
        val lineBackground = ContextCompat.getColor(context, R.color.line_color)
        mProgressHeight =
            context.resources.getDimensionPixelOffset(R.dimen.progress_video_line_height)
        mBackgroundColor.isAntiAlias = true
        mBackgroundColor.color = lineBackground
        mProgressColor.isAntiAlias = true
        mProgressColor.color = lineProgress
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1)
        }
        val minH = paddingBottom + paddingTop + mProgressHeight
        var viewHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1)
        }
        setMeasuredDimension(mViewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLineBackground(canvas)
        drawLineProgress(canvas)
    }

    private fun drawLineBackground(canvas: Canvas) {
        if (mBackgroundRect != null) {
            canvas.drawRect(mBackgroundRect!!, mBackgroundColor)
        }
    }

    private fun drawLineProgress(canvas: Canvas) {
        if (mProgressRect != null) {
            canvas.drawRect(mProgressRect!!, mProgressColor)
        }
    }

    override fun onCreate(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
        updateBackgroundRect(index, value)
    }

    override fun onSeek(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
        updateBackgroundRect(index, value)
    }

    override fun onSeekStart(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
        updateBackgroundRect(index, value)
    }

    override fun onSeekStop(rangeSeekBarView: RangeSeekBarView?, index: Int, value: Float) {
        updateBackgroundRect(index, value)
    }

    private fun updateBackgroundRect(index: Int, value: Float) {
        if (mBackgroundRect == null) {
            mBackgroundRect = Rect(0, 0, mViewWidth, mProgressHeight)
        }
        val newValue = (mViewWidth * value / 100).toInt()
        mBackgroundRect = if (index == 0) {
            Rect(newValue, mBackgroundRect!!.top, mBackgroundRect!!.right, mBackgroundRect!!.bottom)
        } else {
            Rect(mBackgroundRect!!.left, mBackgroundRect!!.top, newValue, mBackgroundRect!!.bottom)
        }
        updateProgress(0, 0, 0.0f)
    }

    override fun updateProgress(time: Int, max: Int, scale: Float) {
        mProgressRect = if (scale == 0f) {
            Rect(0, mBackgroundRect!!.top, 0, mBackgroundRect!!.bottom)
        } else {
            val newValue = (mViewWidth * scale / 100).toInt()
            Rect(mBackgroundRect!!.left, mBackgroundRect!!.top, newValue, mBackgroundRect!!.bottom)
        }
        invalidate()
    }

    init {
        init()
    }
}
