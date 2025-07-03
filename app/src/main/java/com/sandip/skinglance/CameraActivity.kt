package com.sandip.skinglance

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sandip.skinglance.databinding.ActivityCameraBinding
import java.io.ByteArrayOutputStream

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private lateinit var binding: ActivityCameraBinding
    companion object {
        private const val REQUEST_SELECT_IMAGE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startCamera()

        findViewById<ImageView>(R.id.selectImg).setOnClickListener {
            selectImageFromGallery()
        }

        // set on click listener for the button of capture photo
        // it calls a method which is implemented below
        findViewById<ImageView>(R.id.captureImg).setOnClickListener {
            takePhoto()
        }

    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_SELECT_IMAGE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            if (selectedImageUri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)

                // Convert bitmap to byte array
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                // Start the next activity and pass the URI
                val intent = Intent(this@CameraActivity, ResultActivity::class.java).apply {
                    putExtra("cropped_image", byteArray)
                }
                startActivity(intent)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {

            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("SkinCan", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // Get a stable reference of the
        // modifiable image capture use case
        val imageCapture = imageCapture ?: return

        val name = "IMG_${System.currentTimeMillis()}.jpeg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SkinCan")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
        // Set up image capture listener,
        // which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("SkinCan", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.EMPTY
                    val msg = "Photo captured!"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("SkinCan", msg)

                    // Get rectangle coordinates
                    val drawView = findViewById<DrawView>(R.id.drawView)
                    val rect = drawView.getRectangle()

                    // Get the bitmap from the uri
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, savedUri)

                    // Correct the image orientation
                    val correctedBitmap = correctImageOrientation(bitmap, savedUri)

                    val croppedBitmap : Bitmap
                    if (rect.top != 0 && rect.left != 0 && rect.width() != 0 && rect.height() != 0) {
                        // Crop the bitmap based on the rectangle
                        val adjustedRect = adjustRect(rect, correctedBitmap.width, correctedBitmap.height)
                        croppedBitmap = Bitmap.createBitmap(
                            correctedBitmap,
                            adjustedRect.left,
                            adjustedRect.top,
                            adjustedRect.width(),
                            adjustedRect.height()
                        )
                    } else {
                        // If no rectangle is selected, use the original bitmap
                        croppedBitmap = correctedBitmap
                    }

                    // Save the cropped image
                    val croppedUri = saveCroppedImage(croppedBitmap)

                    // Convert bitmap to byte array
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()

                    // Start the next activity and pass the URI
                    val intent = Intent(this@CameraActivity, ResultActivity::class.java).apply {
                        putExtra("cropped_image", byteArray)
                    }
                    startActivity(intent)
                }

            })
    }

    // Function to save the cropped image
    private fun saveCroppedImage(croppedBitmap: Bitmap): Uri {
        val name = "CROPPED_IMG_${System.currentTimeMillis()}.jpeg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SkinCan")
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }
        Toast.makeText(baseContext, "Cropped image saved.", Toast.LENGTH_SHORT).show()
        return uri ?: Uri.EMPTY
    }

    private fun adjustRect(rect: Rect, bitmapWidth: Int, bitmapHeight: Int): Rect {
        val scaleX = bitmapWidth.toFloat() / binding.viewFinder.width
        val scaleY = bitmapHeight.toFloat() / binding.viewFinder.height

        val adjustedLeft = (rect.left * scaleX).toInt()
        val adjustedTop = (rect.top * scaleY).toInt()
        val adjustedRight = (rect.right * scaleX).toInt()
        val adjustedBottom = (rect.bottom * scaleY).toInt()

        return Rect(adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)
    }

    private fun correctImageOrientation(bitmap: Bitmap, imageUri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(imageUri)
        val ei = inputStream?.let { ExifInterface(it) }
        val orientation = ei?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        inputStream?.close()
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}