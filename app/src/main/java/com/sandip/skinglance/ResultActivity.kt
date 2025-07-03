package com.sandip.skinglance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class ResultActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var resultMessageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        imageView = findViewById(R.id.result_imageView)
        resultTextView = findViewById(R.id.result_textView)
        resultMessageTextView = findViewById(R.id.result_message_textView)

        // Retrieve the byte array from the intent
        val byteArray = intent.getByteArrayExtra("cropped_image")
        var bitmap: Bitmap? = null
        byteArray?.let {
            // Convert byte array back to bitmap
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            imageView.setImageBitmap(bitmap)
            resultTextView.text = "Predicting..."

        }

        bitmap?.let {
            // Call Retrofit to send image to the server
            uploadImageToServer(it)
        }
    }

    private fun uploadImageToServer(bitmap: Bitmap) {
        // Create Retrofit instance
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.2:8000") //Replace with actual API request URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(SkinCanApi::class.java)

        // Convert Bitmap to ByteArray
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Create RequestBody for the image
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val part = MultipartBody.Part.createFormData("img", "image.jpg", requestBody)

        // Send the image to the FastAPI server
        apiService.uploadImage(part).enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val resultList = response.body()
                    val result = resultList?.get(0)

                    resultTextView.text = result
                    if(result == "Malignant"){
                        resultTextView.setBackgroundDrawable(getDrawable(R.drawable.result_status_red))
                        resultTextView.setTextColor(getColor(R.color.red))
                        resultMessageTextView.text = getString(R.string.malignant_message)
                    } else {
                        resultMessageTextView.text = getString(R.string.benign_message)
                    }

                } else {
                    // Handle error
                    Toast.makeText(this@ResultActivity, "Prediction failed!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                // Handle failure
                Toast.makeText(this@ResultActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                resultTextView.text = "Error!"
                resultTextView.setBackgroundDrawable(getDrawable(R.drawable.result_status_red))
                resultTextView.setTextColor(getColor(R.color.red))
                resultMessageTextView.text = "Network Error: ${t.message}"
            }
        })
    }
}
