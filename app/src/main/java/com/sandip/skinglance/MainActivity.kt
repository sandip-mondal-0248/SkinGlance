package com.sandip.skinglance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    var scanButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerview = findViewById<RecyclerView>(R.id.article_list)

        recyclerview.layoutManager = LinearLayoutManager(this)

        val data = ArrayList<Article>()


        data.add(Article("Understanding Skin \nCancer", "Skin cancer is one of the most\ncommon forms of cancer worldwide.\nEarly detection can save lives...", R.drawable.article_img_1))
        data.add(Article("ABCDE Rule for Moles", "The ABCDE rule helps identify\nunusual moles: Asymmetry,\nBorder, Color, Diameter, Evolving...", R.drawable.article_img_2))
        data.add(Article("Sun Safety Essentials", "Using sunscreen, wearing protective\nclothing, and avoiding UV exposure\nare key to skin cancer prevention...", R.drawable.article_img_3))
        data.add(Article("Self-Examination Tips", "Monthly skin checks at home\ncan help spot early signs\nof skin abnormalities or growths...", R.drawable.article_img_4))
        data.add(Article("AI in Skin Cancer \nDetection", "Artificial Intelligence is being\nused to assist dermatologists in\nidentifying potential cancerous lesions...", R.drawable.article_img_1))
        data.add(Article("UV Index Awareness", "Monitoring the UV index can\nhelp reduce the risk of\nharmful sun exposure during the day...", R.drawable.article_img_2))
        data.add(Article("Myths About Skin \nCancer", "Skin cancer can affect all\nskin tones and is not\nonly caused by sunburn...", R.drawable.article_img_3))
        data.add(Article("Importance of \nDermatology Visits", "Regular dermatology checkups are\nrecommended for individuals at risk\nor with a history of sun damage...", R.drawable.article_img_4))


        val adapter = ArticlesAdapter(data)
        recyclerview.adapter = adapter

        scanButton= findViewById(R.id.scan_button)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // checks the camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // If all permissions granted , then start Camera
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // If permissions are not granted,
                // present a toast to notify the user that
                // the permissions were not granted.
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        scanButton?.setOnClickListener {
            val intent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "SkinGlance"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
    }
}