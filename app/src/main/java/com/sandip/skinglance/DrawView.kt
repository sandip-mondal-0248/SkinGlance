package com.sandip.skinglance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class DrawView : View {
    var points: Array<Point?> = arrayOfNulls(4)

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    var groupId: Int = -1
    private val colorballs = ArrayList<ColorBall>()

    // array that holds the balls
    private var balID = 0

    // variable to know what ball is being dragged
    var paint: Paint? = null
    var canvas: Canvas? = null

    constructor(context: Context?) : super(context) {
        paint = Paint()
        isFocusable = true // necessary for getting the touch events
        canvas = Canvas()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        paint = Paint()
        isFocusable = true // necessary for getting the touch events
        canvas = Canvas()
    }

    // the method that draws the balls
    override fun onDraw(canvas: Canvas) {
        if (points[3] == null) //point4 null when user did not touch and move on screen.
            return
        var left: Int
        var top: Int
        var right: Int
        var bottom: Int
        left = points[0]!!.x
        top = points[0]!!.y
        right = points[0]!!.x
        bottom = points[0]!!.y
        for (i in 1 until points.size) {
            left = if (left > points[i]!!.x) points[i]!!.x else left
            top = if (top > points[i]!!.y) points[i]!!.y else top
            right = if (right < points[i]!!.x) points[i]!!.x else right
            bottom = if (bottom < points[i]!!.y) points[i]!!.y else bottom
        }
        paint!!.isAntiAlias = true
        paint!!.isDither = true
        paint!!.strokeJoin = Paint.Join.ROUND
        paint!!.strokeWidth = 5f

        //draw stroke
        paint!!.style = Paint.Style.STROKE
        paint!!.color = Color.GREEN
        paint!!.strokeWidth = 2f
        canvas.drawRect(
            (left + colorballs[0].widthOfBall / 2).toFloat(),
            (top + colorballs[0].widthOfBall / 2).toFloat(),
            (right + colorballs[2].widthOfBall / 2).toFloat(),
            (bottom + colorballs[2].widthOfBall / 2).toFloat(), paint!!
        )
        //fill the rectangle
        paint!!.style = Paint.Style.FILL
        paint!!.color = Color.TRANSPARENT
        paint!!.strokeWidth = 0f
        canvas.drawRect(
            (left + colorballs[0].widthOfBall / 2).toFloat(),
            (top + colorballs[0].widthOfBall / 2).toFloat(),
            (right + colorballs[2].widthOfBall / 2).toFloat(),
            (bottom + colorballs[2].widthOfBall / 2).toFloat(), paint!!
        )

        //draw the corners
        val bitmap = BitmapDrawable()
        // draw the balls on the canvas
        paint!!.color = Color.GRAY
        paint!!.textSize = 18f
        paint!!.strokeWidth = 0f
        for (i in colorballs.indices) {
            val ball = colorballs[i]
            canvas.drawBitmap(ball.bitmap, ball.x.toFloat(), ball.y.toFloat(), paint)
        }
    }

    // events when touching the screen
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventaction = event.action

        val X = event.x.toInt()
        val Y = event.y.toInt()

        when (eventaction) {
            MotionEvent.ACTION_DOWN ->                 // a ball
                if (points[0] == null) {
                    //initialize rectangle.
                    points[0] = Point()
                    points[0]!!.x = X
                    points[0]!!.y = Y

                    points[1] = Point()
                    points[1]!!.x = X
                    points[1]!!.y = Y + 30

                    points[2] = Point()
                    points[2]!!.x = X + 30
                    points[2]!!.y = Y + 30

                    points[3] = Point()
                    points[3]!!.x = X + 30
                    points[3]!!.y = Y

                    balID = 2
                    groupId = 1
                    // declare each ball with the ColorBall class
                    for (pt in points) {
                        colorballs.add(ColorBall(context, R.drawable.ic_circle, pt))
                    }
                } else {
                    //resize rectangle
                    balID = -1
                    groupId = -1
                    var i = colorballs.size - 1
                    while (i >= 0) {
                        val ball = colorballs[i]
                        // check if inside the bounds of the ball (circle)
                        // get the center for the ball
                        val centerX = ball.x + ball.widthOfBall
                        val centerY = ball.y + ball.heightOfBall
                        paint!!.color = Color.CYAN
                        // calculate the radius from the touch to the center of the
                        // ball
                        val radCircle = sqrt(
                            (((centerX - X) * (centerX - X)) + (centerY - Y)
                                    * (centerY - Y)).toDouble()
                        )

                        if (radCircle < ball.widthOfBall) {
                            balID = ball.iD
                            groupId = if (balID == 1 || balID == 3) {
                                2
                            } else {
                                1
                            }
                            invalidate()
                            break
                        }
                        invalidate()
                        i--
                    }
                }

            MotionEvent.ACTION_MOVE -> if (balID > -1) {
                // move the balls the same as the finger
                colorballs[balID].x = X
                colorballs[balID].y = Y

                paint!!.color = Color.CYAN
                if (groupId == 1) {
                    colorballs[1].x = colorballs[0].x
                    colorballs[1].y = colorballs[2].y
                    colorballs[3].x = colorballs[2].x
                    colorballs[3].y = colorballs[0].y
                } else {
                    colorballs[0].x = colorballs[1].x
                    colorballs[0].y = colorballs[3].y
                    colorballs[2].x = colorballs[3].x
                    colorballs[2].y = colorballs[1].y
                }

                invalidate()
            }

            MotionEvent.ACTION_UP -> {}
        }
        // redraw the canvas
        invalidate()
        return true
    }

    fun getRectangle(): Rect {
        val left = points.minOf { it?.x ?: 0 }
        val top = points.minOf { it?.y ?: 0 }
        val right = points.maxOf { it?.x ?: 0 }
        val bottom = points.maxOf { it?.y ?: 0 }

        return Rect(left, top, right, bottom)
    }


    class ColorBall(context: Context, resourceId: Int, point: Point?) {
        var bitmap: Bitmap
        var mContext: Context
        var point: Point?
        var iD: Int

        init {
            this.iD = count++
            bitmap = BitmapFactory.decodeResource(
                context.resources,
                resourceId
            )
            bitmap = Bitmap.createScaledBitmap(bitmap, 30, 30, false)
            mContext = context
            this.point = point
        }

        val widthOfBall: Int
            get() = bitmap.width

        val heightOfBall: Int
            get() = bitmap.height

        var x: Int
            get() = point!!.x
            set(x) {
                point!!.x = x
            }

        var y: Int
            get() = point!!.y
            set(y) {
                point!!.y = y
            }

        companion object {
            var count: Int = 0
        }
    }
}
