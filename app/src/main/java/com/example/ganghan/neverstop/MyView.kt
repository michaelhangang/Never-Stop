package com.example.ganghan.neverstop

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import java.util.*
import kotlin.collections.ArrayList

/**
 * TODO: document your custom view class.
 */
class MyView : View {
   companion object{
       var myBitmaps = ArrayList<Bitmap>()
       var leftPositionY = listOf(200F,400F,600F,800F,1000F,1200F,1400F)

   }
    var isRenderLeft :Int = 0
    var isDisppear = true
    var width: Float = 0.0F
    var y: Int = 0
    var leftIndex: PoseLandmark? = null
    var rightIndex: PoseLandmark? = null
    private var  whitePaint = Paint()
    private var leftPaint = Paint()
    private var score =0

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.MyView, defStyle, 0
        )
        whitePaint.color = Color.WHITE
        leftPaint.color = Color.GREEN
        var bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gold)
        var myBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
        var flames_icebitmap = BitmapFactory.decodeResource(getResources(), R.drawable.magic_circle)
        var flames_iceb = Bitmap.createScaledBitmap(flames_icebitmap, 200, 200, false)
        myBitmaps.add(myBitmap!!)
        myBitmaps.add(flames_iceb!!)
    }

    override fun onDraw(canvas: Canvas) {
//        if(myBitmap==null){
////            var bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gold)
////            myBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
//
//        }
        if(isDisppear) {
            // Randomly draw the graphic
            isRenderLeft = (0..1).random()
             y = (0..6).random()
            isDisppear = false
        }

        if (isRenderLeft == 0) {
            canvas.drawBitmap(myBitmaps[0], 0F, leftPositionY[y], whitePaint)
        } else {
            canvas.drawBitmap(myBitmaps[0], width - 200, leftPositionY[y], whitePaint)
        }

        // Clear the graphic
        if(leftIndex!=null&&rightIndex!=null){
            if( leftIndex!!.position.x >width - 200F && (leftIndex!!.position.y <leftPositionY[y]+200F &&leftIndex!!.position.y>leftPositionY[y])){
               clear(canvas)
            }
            else if(rightIndex!!.position.x < 200 && (rightIndex!!.position.y <leftPositionY[y]+200F&&rightIndex!!.position.y >leftPositionY[y])){
               clear(canvas)
            }
//            Log.i("CameraXBasic",leftPositionY[y].toString() + " < "+leftIndex!!.position.y.toString() + " < " +(leftPositionY[y]+200F).toString() )
//            Log.i("CameraXBasic",rightIndex!!.position.x.toString() +" > " + (width - 200).toString() )

        }
        if(leftIndex!=null&&rightIndex!=null) {
            canvas.drawBitmap(myBitmaps[1], translateX(leftIndex!!.position.x), leftIndex!!.position.y, whitePaint)
            canvas.drawBitmap(myBitmaps[1], translateX(rightIndex!!.position.x), rightIndex!!.position.y, whitePaint)
        }
//        drawPoint(canvas,leftIndex?.position,whitePaint)
//        drawPoint(canvas,rightIndex?.position,whitePaint)
    }

    fun drawPoint(canvas: Canvas, point: PointF?, paint: Paint?) {
        if (point == null) {
            return
        }
        canvas.drawCircle(
                translateX(point.x),
                point.y,
                40F,
               paint!!
        )
    }
    fun drawLine(canvas: Canvas, start: PointF?, end: PointF?, paint: Paint?) {
        if (start == null || end == null) {
            return
        }
        canvas.drawLine(
                translateX(start.x), start.y, translateX(end.x), end.y, paint!!
        )
    }
    fun translateX(ponit:Float):Float{
        return (width - ponit)
    }
    fun clear(canvas: Canvas){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        isDisppear = true
    }
}