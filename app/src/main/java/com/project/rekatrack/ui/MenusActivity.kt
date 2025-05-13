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
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "MenusActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate called")

        tokenHandler = TokenHandler(this)

        val userName = tokenHandler.getUserName()
        val userRole = tokenHandler.getUserRole()

        binding.tvUserName.text = "Halo, $userName"
        binding.tvUserRole.text = userRole

        Log.d(TAG, "User: $userName, Role: $userRole")


        cameraPermissionLauncher = registerForActivityResult(
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

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Lokasi diizinkan", Toast.LENGTH_LONG).show()
                // Lanjutkan ke fungsi yang membutuhkan akses lokasi
            } else {
                Toast.makeText(this, "Lokasi tidak diizinkan", Toast.LENGTH_LONG).show()
            }
        }


        binding = ActivityMenusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "View binding selesai")

        cameraPermission()
        locationPermission()

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

    private fun locationPermission() {
        Log.d(TAG, "Cek permission kamera")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Lokasi sudah diizinkan
            Log.d(TAG, "Lokasi diizinkan")
            Toast.makeText(this, "Lokasi diizinkan", Toast.LENGTH_SHORT).show()
        } else {
            // Lokasi belum diizinkan, meminta izin
            Log.d(TAG, "Lokasi belum diizinkan, meminta izin")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun cameraPermission() {
        Log.d(TAG, "Cek permission kamera")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Kamera sudah diizinkan
            Log.d(TAG, "Kamera diizinkan")
            Toast.makeText(this, "Kamera diizinkan", Toast.LENGTH_SHORT).show()
        } else {
            // Kamera belum diizinkan, meminta izin
            Log.d(TAG, "Kamera belum diizinkan, meminta izin")
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCameraActivity() {
        Log.d(TAG, "Meluncurkan CameraActivity")
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}
