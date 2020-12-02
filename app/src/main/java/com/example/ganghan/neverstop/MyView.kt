package com.example.ganghan.neverstop

import android.content.Context
import android.graphics.*
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.sample_my_view.view.*
import java.time.LocalTime

/**
 * TODO: document your custom view class.
 */
class MyView : View {
   companion object{
       var myBitmaps = ArrayList<Bitmap>()
       var leftPositionY = listOf(200F,400F,600F,800F,1000F,1200F,1400F)
       var handEffect = 0
       var min:Long = 1;
       var isUseTimer = false
   }
    var ready = listOf("","","3","2","1","Go","")
    var readyIndex = 0
    var currentCountDown = ""
    var isRenderLeft :Int = 0
    var isDisppear = true
    var width: Float = 0.0F
    var y: Int = 0
    var isStart = false
    var leftIndex: PoseLandmark? = null
    var rightIndex: PoseLandmark? = null
    private var  whitePaint = Paint()
    private var readyPaint = Paint()
    private var score =0
    private var flag =1
    private val mainHandler = Handler(Looper.getMainLooper())
    private var timer1 = object: CountDownTimer(min*60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
           // val hours = millisUntilFinished / (1000 * 60 * 60) % 24
            val minutes = millisUntilFinished / (1000 * 60) % 60
            val seconds = (millisUntilFinished / 1000) % 60
            currentCountDown = minutes.toString() + ":" + seconds.toString()
        }

        override fun onFinish() {
            flag =3

        }
    }
    private var timer = object: CountDownTimer(6000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
           if( ready[readyIndex]!=null)
               currentCountDown =ready[readyIndex]
               readyIndex++
        }

        override fun onFinish() {
            if(isUseTimer){
                timer1.start()
                flag =2

            }else
                currentCountDown =""
            isStart=true
        }
    }
    // String
    private var _exampleString: String? = null // TODO: use a default from R.string...
    private var _exampleColor: Int = Color.WHITE // TODO: use a default from R.color...
    private var _exampleDimension: Float = 30f // TODO: use a default from R.dimen...

    private lateinit var textPaint: TextPaint
    private var textWidth: Float = 0f
    private var textHeight: Float = 0f
    /**
     * The text to draw
     */
    var exampleString: String?
        get() = _exampleString
        set(value) {
            _exampleString = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * The font color
     */
    var exampleColor: Int
        get() = _exampleColor
        set(value) {
            _exampleColor = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this dimension is the font size.
     */
    var exampleDimension: Float
        get() = _exampleDimension
        set(value) {
            _exampleDimension = value
            invalidateTextPaintAndMeasurements()
        }


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
        whitePaint.style  = Paint.Style.FILL
        whitePaint.textSize = 150F
        readyPaint.color = Color.YELLOW
        readyPaint.style  = Paint.Style.FILL
        readyPaint.textSize = 300F

        var bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gold)
        var myBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
        var flames_icebitmap = BitmapFactory.decodeResource(getResources(), R.drawable.magic_circle)
        var flames_iceb = Bitmap.createScaledBitmap(flames_icebitmap, 230, 230, false)
        var kiss_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kisspng)
        var kiss = Bitmap.createScaledBitmap(kiss_bitmap, 230, 230, false)

        myBitmaps.add(myBitmap!!)
        myBitmaps.add(flames_iceb!!)
        myBitmaps.add(kiss!!)

        // string
        _exampleString = a.getString(
            R.styleable.MyView_exampleString)
        _exampleColor = a.getColor(
            R.styleable.MyView_exampleColor,
            exampleColor)
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        _exampleDimension = a.getDimension(
            R.styleable.MyView_exampleDimension,
            exampleDimension)
        a.recycle()
        // Set up a default TextPaint object
        textPaint = TextPaint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textAlign = Paint.Align.LEFT
        }
        exampleString = "0"
        // Update TextPaint and text measurements from attributes
        timer.start()
    }
//    private val timer = object : Runnable {
//        override fun run() {
//            addScore()
//            mainHandler.postDelayed(this, 1000)
//        }
//    }
    private fun invalidateTextPaintAndMeasurements() {
        textPaint.let {
            it.textSize = exampleDimension
            it.color = exampleColor
            textWidth = it.measureText(exampleString)
            textHeight = it.fontMetrics.bottom
        }
    }
    override fun onDraw(canvas: Canvas) {
        if(isDisppear) {
            // Randomly draw the graphic
            isRenderLeft = (0..1).random()
             y = (0..6).random()
            isDisppear = false
        }
        if(isStart) {
            if (isRenderLeft == 0) {
                canvas.drawBitmap(myBitmaps[0], 0F, leftPositionY[y], whitePaint)
            } else {
                canvas.drawBitmap(myBitmaps[0], width - 200, leftPositionY[y], whitePaint)
            }
        }
        // Clear the graphic
        if(leftIndex!=null&&rightIndex!=null){
            if( leftIndex!!.position.x >width - 200F && (leftIndex!!.position.y <leftPositionY[y]+200F &&leftIndex!!.position.y>leftPositionY[y])){
               clear(canvas)
               addScore()
            }
            else if(rightIndex!!.position.x < 200 && (rightIndex!!.position.y <leftPositionY[y]+200F&&rightIndex!!.position.y >leftPositionY[y])){
               clear(canvas)
                addScore()

            }

        }
        if(leftIndex!=null&&rightIndex!=null) {
            canvas.drawBitmap(myBitmaps[handEffect], translateX(leftIndex!!.position.x), leftIndex!!.position.y, whitePaint)
            canvas.drawBitmap(myBitmaps[handEffect], translateX(rightIndex!!.position.x), rightIndex!!.position.y, whitePaint)
        }

        // Draw string
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom
        exampleString =score.toString()
        exampleString?.let {
            // Draw the text.
            canvas.drawText(it, 590F, 230F, textPaint)
        }
        when(flag){
            1->canvas.drawText(currentCountDown, 450F, 730F, readyPaint)
            2-> canvas.drawText(currentCountDown, 590F, 130F, textPaint)
            3-> canvas.drawText("Time is up!", 230F, 730F, whitePaint)
        }
    }

//    fun drawPoint(canvas: Canvas, point: PointF?, paint: Paint?) {
//        if (point == null) {
//            return
//        }
//        canvas.drawCircle(
//                translateX(point.x),
//                point.y,
//                40F,
//               paint!!
//        )
//    }
//    fun drawLine(canvas: Canvas, start: PointF?, end: PointF?, paint: Paint?) {
//        if (start == null || end == null) {
//            return
//        }
//        canvas.drawLine(
//                translateX(start.x), start.y, translateX(end.x), end.y, paint!!
//        )
//    }
    fun translateX(ponit:Float):Float{
        return (width - ponit)
    }
    fun clear(canvas: Canvas){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
        isDisppear = true
    }
    fun addScore(){
        score +=100
    }

}