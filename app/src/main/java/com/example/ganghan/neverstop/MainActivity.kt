package com.example.ganghan.neverstop

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var myView: MyView
    private  var width= 0
    private  var height= 0
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var poseDetector : PoseDetector;
    private lateinit var luminosityAnalyzer:poseAnalyzer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        myView = findViewById(R.id.myCustomView)
        // Set up the listener for take photo button
//        camera_capture_button.setOnClickListener { takePhoto() }
        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Base pose detector with streaming frames, when depending on the pose-detection sdk
        val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
        poseDetector = PoseDetection.getClient(options)
        luminosityAnalyzer = poseAnalyzer()
        // get the width and height of the screen
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            var imageCapture = ImageCapture.Builder().build()
//   Please replace the width and height of size in setTargetResolution method with your device's width and height
            //.setTargetResolution(Size(1080, 1920))
            val imageAnalyzer = ImageAnalysis.Builder().setTargetResolution(Size(width, height))
                .setBackpressureStrategy(
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                            var image = InputImage.fromMediaImage(
                                imageProxy.image!!,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            // insert your code here.
                            poseDetector.process(image)
                                .addOnSuccessListener(cameraExecutor) { results ->
                                    val leftIndex =
                                        results.getPoseLandmark(PoseLandmark.LEFT_INDEX)
                                    val rightIndex =
                                        results.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
                                    myView.leftIndex = leftIndex
                                    myView.rightIndex = rightIndex
                                    myView.width = width.toFloat()
                                    myView.invalidate()
                                }
                                .addOnFailureListener { e ->
                                    Log.i(TAG, e.toString())
                                }
                                .addOnCompleteListener { results -> imageProxy.close() }
                        })
                }
            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    inner  class poseAnalyzer: ImageAnalysis.Analyzer {
        //    @SuppressLint("UnsafeExperimentalUsageError")
        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            var image = InputImage.fromMediaImage(
                imageProxy.image!!,
                imageProxy.imageInfo.rotationDegrees
            )
            // insert your code here.
            poseDetector.process(image)
                .addOnSuccessListener(cameraExecutor) { results ->
                    val left_wrist = results.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                    val right_wrist = results.getPoseLandmark(PoseLandmark.RIGHT_WRIST)

                    if (left_wrist !=null &&right_wrist!=null){
                        //   Log.i(TAG, nose.position.toString())
//                        myView.leftIndex = leftIndex
//                        myView.rightIndex = rightIndex
                        myView.invalidate()
                    }

                }
                .addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
                .addOnCompleteListener{ results -> imageProxy.close()}
        }

    }
}
