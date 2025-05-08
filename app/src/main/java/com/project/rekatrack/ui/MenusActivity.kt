package com.project.rekatrack.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.rekatrack.databinding.ActivityMenusBinding
import com.project.rekatrack.support.TokenHandler

class MenusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenusBinding
    private lateinit var tokenHandler: TokenHandler
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "MenusActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called")

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Log.d(TAG, "Permission result: $isGranted")
            if (isGranted) {
                Toast.makeText(this, "Kamera diizinkan", Toast.LENGTH_LONG).show()
//                launchCameraActivity()
            } else {
                Toast.makeText(this, "Kamera tidak diizinkan", Toast.LENGTH_LONG).show()
            }
        }

        binding = ActivityMenusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "View binding selesai")

        cameraPermission()

        tokenHandler = TokenHandler(this)
        Log.d(TAG, "Token: ${tokenHandler.getToken()}")

        binding.btnAbout.setOnClickListener {
            Log.d(TAG, "btnAbout diklik")
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.btnTracking.setOnClickListener {
//            Log.d(TAG, "btnTracking diklik")
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                == PackageManager.PERMISSION_GRANTED
//            ) {
//                Log.d(TAG, "Permission kamera SUDAH diberikan, buka CameraActivity")
////                launchCameraActivity()
//            } else {
//                Log.d(TAG, "Permission kamera BELUM diberikan, request launcher")
//                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//            }
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
        }

        binding.btnExit.setOnClickListener {
            Log.d(TAG, "btnExit diklik - Menutup aplikasi")
            finishAffinity()
        }
    }

    private fun cameraPermission() {
        Log.d(TAG, "Cek permission kamera")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Permission kamera TIDAK diberikan, meminta via ActivityCompat")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d(TAG, "Permission kamera SUDAH diberikan")
        }
    }

    private fun launchCameraActivity() {
        Log.d(TAG, "Meluncurkan CameraActivity")
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult dipanggil")
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission kamera DITERIMA via ActivityCompat")
//                    launchCameraActivity()
                } else {
                    Log.d(TAG, "Permission kamera DITOLAK via ActivityCompat")
                }
            }
        }
    }
}
