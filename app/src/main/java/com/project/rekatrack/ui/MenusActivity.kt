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
import androidx.lifecycle.ViewModelProvider
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.viewModel.GeneralViewModel
import com.project.rekatrack.databinding.ActivityMenusBinding
import com.project.rekatrack.network.ApiConfig
import com.project.rekatrack.support.SessionHandler
import com.project.rekatrack.support.TokenHandler
import com.project.rekatrack.viewModelFactory.ViewModelFactory

class MenusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenusBinding
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var generalViewModel: GeneralViewModel

    companion object {
        private const val TAG = "MenusActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenHandler = TokenHandler(this)

        val repository = Repository(ApiConfig.getApiService(tokenHandler))
        val factory = ViewModelFactory(repository, this)
        generalViewModel = ViewModelProvider(this, factory).get(GeneralViewModel::class.java)

        val userName = tokenHandler.getUserName()
        val userRole = tokenHandler.getUserRole()

        Log.d(TAG, "User: $userName, Role: $userRole")


        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Log.d(TAG, "Permission result: $isGranted")
            if (isGranted) {
                Toast.makeText(this, "Kamera diizinkan", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Kamera tidak diizinkan", Toast.LENGTH_LONG).show()
            }
            locationPermission()
        }

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notifikasi tidak diizinkan", Toast.LENGTH_SHORT).show()
            }
        }

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Lokasi diizinkan", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Lokasi tidak diizinkan", Toast.LENGTH_LONG).show()
            }
        }


        binding = ActivityMenusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraPermission()

        SessionHandler.onSessionExpired = {
            generalViewModel.onSessionExpired()
        }

        generalViewModel.sessionExpired.observe(this) { isExpired ->
            if (isExpired) {
                Toast.makeText(this, "Session expired, silakan login kembali", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        binding.tvUserName.text = "Halo, $userName"
        binding.tvUserRole.text = userRole


        binding.btnAbout.setOnClickListener {
            Log.d(TAG, "btnAbout diklik")
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.btnTracking.setOnClickListener {
            val intent = Intent(this, TrackingActivity::class.java)
            startActivity(intent)
        }

        binding.btnExit.setOnClickListener {
            generalViewModel.authLogout()

            tokenHandler.removeToken()

            generalViewModel.logoutResponse.observe(this) { response ->
                if (response.message == "Success!") {
                    finishAffinity()
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun locationPermission() {
        Log.d(TAG, "Cek permission kamera")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Lokasi diizinkan", Toast.LENGTH_SHORT).show()
        } else {
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
            Toast.makeText(this, "Kamera diizinkan", Toast.LENGTH_SHORT).show()
            locationPermission()
        } else {
            Log.d(TAG, "Kamera belum diizinkan, meminta izin")
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

}
