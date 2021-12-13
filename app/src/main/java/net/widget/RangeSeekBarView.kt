package net.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import net.basicmodel.R
import net.entity.Thumb
import net.interfaces.OnRangeSeekBarListener
import java.util.*

class RangeSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var mHeightTimeLine = 0
    private var mThumbs: List<Thumb>? = null
    private var mListeners: MutableList<OnRangeSeekBarListener>? = null
    private var mMaxWidth = 0f
    private var mThumbWidth = 0f
    private var mThumbHeight = 0f
    private var mViewWidth = 0
    private var mPixelRangeMin = 0f
    private var mPixelRangeMax = 0f
    private var mScaleRangeMax = 0f
    private var mFirstRun = false
    private val mShadow = Paint()
    private val mLine = Paint()
    private fun init() {
        mThumbs = Thumb.initThumbs(resources)
        mThumbWidth = Thumb.getWidthBitmap(mThumbs!!).toFloat()
        mThumbHeight = Thumb.getHeightBitmap(mThumbs!!).toFloat()
        mScaleRangeMax = 100f
        mHeightTimeLine = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
        isFocusable = true
        isFocusableInTouchMode = true
        mFirstRun = true
        val shadowColor = ContextCompat.getColor(context, R.color.shadow_color)
        mShadow.isAntiAlias = true
        mShadow.color = shadowColor
        mShadow.alpha = 177
        val lineColor = ContextCompat.getColor(context, R.color.line_color)
        mLine.isAntiAlias = true
        mLine.color = lineColor
        // mLine.setAlpha(200);
    }

    fun initMaxWidth() {
        mMaxWidth = mThumbs!![1].pos - mThumbs!![0].pos
        onSeekStop(this, 0, mThumbs!![0].getVal())
        onSeekStop(this, 1, mThumbs!![1].getVal())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1)
        }
        val minH = paddingBottom + paddingTop + mThumbHeight.toInt() + mHeightTimeLine
        var viewHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1)
        }
        setMeasuredDimension(mViewWidth, viewHeight)
        mPixelRangeMin = 0f
        mPixelRangeMax = mViewWidth - mThumbWidth
        if (mFirstRun) {
            for (i in mThumbs!!.indices) {
                val th: Thumb = mThumbs!![i]
                th.setVal(mScaleRangeMax * i)
                th.pos = mPixelRangeMax * i
            }
            // Fire listener callback
            onCreate(this, currentThumb, getThumbValue(currentThumb))
            mFirstRun = false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawShadow(canvas)
        drawThumbs(canvas)
    }

    private var currentThumb = 0
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val mThumb: Thumb
        val mThumb2: Thumb
        val coordinate = ev.x
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {

                // Remember where we started
                currentThumb = getClosestThumb(coordinate)
                if (currentThumb == -1) {
                    return false
                }
                mThumb = mThumbs!![currentThumb]
                mThumb.lastTouchX = coordinate
                onSeekStart(this, currentThumb, mThumb.getVal())
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentThumb == -1) {
                    return false
                }
                mThumb = mThumbs!![currentThumb]
                onSeekStop(this, currentThumb, mThumb.getVal())
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mThumb = mThumbs!![currentThumb]
                mThumb2 = mThumbs!![if (currentThumb == 0) 1 else 0]
                // Calculate the distance moved
                val dx: Float = coordinate - mThumb.lastTouchX
                val newX: Float = mThumb.pos + dx
                if (currentThumb == 0) {
                    if (newX + mThumb.widthBitmap >= mThumb2.pos) {
                        mThumb.pos = mThumb2.pos - mThumb.widthBitmap
                    } else if (newX <= mPixelRangeMin) {
                        mThumb.pos = mPixelRangeMin
                    } else {
                        //Check if thumb is not out of max width
                        checkPositionThumb(mThumb, mThumb2, dx, true)
                        // Move the object
                        mThumb.pos = mThumb.pos + dx

                        // Remember this touch position for the next move event
                        mThumb.lastTouchX = coordinate
                    }
                } else {
                    if (newX <= mThumb2.pos + mThumb2.widthBitmap) {
                        mThumb.pos = mThumb2.pos + mThumb.widthBitmap
                    } else if (newX >= mPixelRangeMax) {
                        mThumb.pos = mPixelRangeMax
                    } else {
                        //Check if thumb is not out of max width
                        checkPositionThumb(mThumb2, mThumb, dx, false)
                        // Move the object
                        mThumb.pos = mThumb.pos + dx
                        // Remember this touch position for the next move event
                        mThumb.lastTouchX = coordinate
                    }
                }
                setThumbPos(currentThumb, mThumb.pos)

                // Invalidate to request a redraw
                invalidate()
                return true
            }
        }
        return false
    }

    private fun checkPositionThumb(
        mThumbLeft: Thumb,
        mThumbRight: Thumb,
        dx: Float,
        isLeftMove: Boolean
    ) {
        if (isLeftMove && dx < 0) {
            if (mThumbRight.pos - (mThumbLeft.pos + dx) > mMaxWidth) {
                mThumbRight.pos = mThumbLeft.pos + dx + mMaxWidth
                setThumbPos(1, mThumbRight.pos)
            }
        } else if (!isLeftMove && dx > 0) {
            if (mThumbRight.pos + dx - mThumbLeft.pos > mMaxWidth) {
                mThumbLeft.pos = mThumbRight.pos + dx - mMaxWidth
                setThumbPos(0, mThumbLeft.pos)
            }
        }
    }

    private fun getUnstuckFrom(index: Int): Int {
        val unstuck = 0
        val lastVal: Float = mThumbs!![index].getVal()
        for (i in index - 1 downTo 0) {
            val th: Thumb = mThumbs!![i]
            if (th.getVal() !== lastVal) return i + 1
        }
        return unstuck
    }

    private fun pixelToScale(index: Int, pixelValue: Float): Float {
        val scale = pixelValue * 100 / mPixelRangeMax
        return if (index == 0) {
            val pxThumb = scale * mThumbWidth / 100
            scale + pxThumb * 100 / mPixelRangeMax
        } else {
            val pxThumb = (100 - scale) * mThumbWidth / 100
            scale - pxThumb * 100 / mPixelRangeMax
        }
    }

    private fun scaleToPixel(index: Int, scaleValue: Float): Float {
        val px = scaleValue * mPixelRangeMax / 100
        return if (index == 0) {
            val pxThumb = scaleValue * mThumbWidth / 100
            px - pxThumb
        } else {
            val pxThumb = (100 - scaleValue) * mThumbWidth / 100
            px + pxThumb
        }
    }

    private fun calculateThumbValue(index: Int) {
        if (index < mThumbs!!.size && !mThumbs!!.isEmpty()) {
            val th: Thumb = mThumbs!![index]
            th.setVal(pixelToScale(index, th.pos))
            onSeek(this, index, th.getVal())
        }
    }

    private fun calculateThumbPos(index: Int) {
        if (index < mThumbs!!.size && !mThumbs!!.isEmpty()) {
            val th: Thumb = mThumbs!![index]
            th.pos = scaleToPixel(index, th.getVal())
        }
    }

    private fun getThumbValue(index: Int): Float {
        return mThumbs!![index].getVal()
    }

    fun setThumbValue(index: Int, value: Float) {
        mThumbs!![index].setVal(value)
        calculateThumbPos(index)
        // Tell the view we want a complete redraw
        invalidate()
    }

    private fun setThumbPos(index: Int, pos: Float) {
        mThumbs!![index].pos = pos
        calculateThumbValue(index)
        // Tell the view we want a complete redraw
        invalidate()
    }

    private fun getClosestThumb(coordinate: Float): Int {
        var closest = -1
        if (!mThumbs!!.isEmpty()) {
            for (i in mThumbs!!.indices) {
                // Find thumb closest to x coordinate
                val tcoordinate: Float = mThumbs!![i].pos + mThumbWidth
                if (coordinate >= mThumbs!![i].pos && coordinate <= tcoordinate) {
                    closest = mThumbs!![i].index
                }
            }
        }
        return closest
    }

    private fun drawShadow(canvas: Canvas) {
        if (!mThumbs!!.isEmpty()) {
            for (th in mThumbs!!) {
                if (th.index === 0) {
                    val x: Float = th.pos + paddingLeft
                    if (x > mPixelRangeMin) {
                        val mRect = Rect(
                            mThumbWidth.toInt(), 0,
                            (x + mThumbWidth).toInt(), mHeightTimeLine
                        )
                        canvas.drawRect(mRect, mShadow)
                    }
                } else {
                    val x: Float = th.pos - paddingRight
                    if (x < mPixelRangeMax) {
                        val mRect = Rect(
                            x.toInt(), 0,
                            (mViewWidth - mThumbWidth).toInt(), mHeightTimeLine
                        )
                        canvas.drawRect(mRect, mShadow)
                    }
                }
            }
        }
    }

    private fun drawThumbs(canvas: Canvas) {
        if (!mThumbs!!.isEmpty()) {
            for (th in mThumbs!!) {
                if (th.index === 0) {
                    canvas.drawBitmap(
                        th.bitmap,
                        (th.pos + paddingLeft).toFloat(),
                        (paddingTop + mHeightTimeLine).toFloat(),
                        null
                    )
                } else {
                    canvas.drawBitmap(
                        th.bitmap,
                        (th.pos - paddingRight).toFloat(),
                        (paddingTop + mHeightTimeLine).toFloat(),
                        null
                    )
                }
            }
        }
    }

    fun addOnRangeSeekBarListener(listener: OnRangeSeekBarListener) {
        if (mListeners == null) {
            mListeners = ArrayList<OnRangeSeekBarListener>()
        }
        mListeners!!.add(listener)
    }

    private fun onCreate(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onCreate(rangeSeekBarView, index, value)
        }
    }

    private fun onSeek(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onSeek(rangeSeekBarView, index, value)
        }
    }

    private fun onSeekStart(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onSeekStart(rangeSeekBarView, index, value)
        }
    }

    private fun onSeekStop(rangeSeekBarView: RangeSeekBarView, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onSeekStop(rangeSeekBarView, index, value)
        }
    }

    val thumbs: List<Any>?
        get() = mThumbs

    companion object {
        private val TAG = RangeSeekBarView::class.java.simpleName
    }

    init {
        init()
    }
}
