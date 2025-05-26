package com.project.rekatrack.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.chip.Chip
import com.project.rekatrack.R
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.response.TravelDocumentInfo
import com.project.rekatrack.data.viewModel.GeneralViewModel
import com.project.rekatrack.databinding.ActivityTrackingBinding
import com.project.rekatrack.network.ApiConfig
import com.project.rekatrack.support.TokenHandler
import com.project.rekatrack.support.TrackingService
import com.project.rekatrack.viewModelFactory.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackingActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTrackingBinding
    private lateinit var generalViewModel: GeneralViewModel
    private lateinit var scanLauncher: ActivityResultLauncher<Intent>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false
    private var trackingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val tokenHandler = TokenHandler(this)
        val token = tokenHandler.getToken() ?: ""

        val repository = Repository(ApiConfig.getApiService(token))
        val factory = ViewModelFactory(repository, this)
        generalViewModel = ViewModelProvider(this, factory).get(GeneralViewModel::class.java)

        scanLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val scannedData = result.data?.getStringExtra("RESULT")
                scannedData?.let {
                    fetchSuratJalan(scannedData)
                }
            }
        }

        binding.btnAddSuratJalan.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            scanLauncher.launch(intent)
        }

        binding.btnStartTracker.setOnClickListener {
            isTracking = !isTracking
            if (isTracking) {
                binding.btnStartTracker.text = "Matikan Tracker"
                binding.btnAddSuratJalan.isEnabled = false // Disable tombol
//                getLocation()
                startLocationUpdates()
            } else {
                binding.btnStartTracker.text = "Hidupkan Tracker"
//                binding.btnAddSuratJalan.isEnabled = true // Enable tombol kembali
                updateStatus()
            }

            //uji foreground
//            isTracking = !isTracking
//            if (isTracking) {
//                binding.btnStartTracker.text = "Matikan Tracker"
//                binding.btnAddSuratJalan.isEnabled = false
//                startTrackingService()
//            } else {
//                binding.btnStartTracker.text = "Hidupkan Tracker"
//                stopTrackingService()
//            }
        }

        binding.btnStopTracker.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Penyelesaian")
                .setMessage("Apakah Anda yakin ingin menyelesaikan pengiriman? Setelah ini Anda tidak dapat menambahkan surat jalan atau mengaktifkan pelacak lagi.")
                .setPositiveButton("Ya") { _, _ ->
                    binding.btnAddSuratJalan.isEnabled = false
                    binding.btnStartTracker.isEnabled = false
                    completeTrackingActivity()
                }
                .setNegativeButton("Batal", null)
                .show()

            //uji foreground
//            binding.btnAddSuratJalan.isEnabled = false
//            binding.btnStartTracker.isEnabled = false
//            stopTrackingService()
        }
    }

    private fun completeTrackingActivity() {
        val travelDocumentIds = generalViewModel.travelDocumentInfoList.value
            ?.mapNotNull { it.id } ?: emptyList()

        if (travelDocumentIds.isEmpty()) {
            Toast.makeText(this, "Tidak ada dokumen untuk diselesaikan", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Izin lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    generalViewModel.completeTrackingActivity(travelDocumentIds, latitude, longitude)
                    generalViewModel.completeTrackingResponse.observe(this) { result ->
                        result?.let {
                            if (it.isNotEmpty()) {
                                val latestStatus = it.last()?.trackingStatus
                                updateStatusTextView(latestStatus)
                                Toast.makeText(this, "Status pengiriman: $latestStatus", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Status kosong", Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            Toast.makeText(this, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
    }


    //uji foreground activity cuy
    private fun startTrackingService() {
        val serviceIntent = Intent(this, TrackingService::class.java)
        startService(serviceIntent)
    }

    private fun stopTrackingService() {
        val serviceIntent = Intent(this, TrackingService::class.java)
        stopService(serviceIntent)
    }

    private fun startLocationUpdates() {
        trackingJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && isTracking) {
                getLocation()
//                delay(5 * 60 * 1000) // 5 menit
                delay(5 * 1000) // 5 menit
            }
        }
    }

    private fun stopLocationUpdates() {
        trackingJob?.cancel()
    }

    private fun updateStatus() {
        val travelDocumentIds = generalViewModel.travelDocumentInfoList.value
            ?.mapNotNull { it.id } ?: emptyList()

        if (travelDocumentIds.isEmpty()) {
            Toast.makeText(this, "Tidak ada dokumen untuk diperbarui", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Izin lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
        .addOnSuccessListener(this) { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                generalViewModel.updateStateTracking(travelDocumentIds, latitude, longitude)

                generalViewModel.updateStateResponse.observe(this) { results ->
                    results?.let {
                        if (it.isNotEmpty()) {
                            val latestStatus = it.last()?.status
                            updateStatusTextView(latestStatus)
                            Toast.makeText(this, "Status diperbarui: $latestStatus", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Status kosong", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun fetchSuratJalan(scannedData: String) {
        generalViewModel.getTravelDocument(scannedData)

        generalViewModel.travelDocumentInfoList.observe(this) { travelDocumentInfoList ->
            binding.chipGroupSuratJalan.removeAllViews()
            binding.chipGroupAlamatPengiriman.removeAllViews()

            travelDocumentInfoList?.forEach { info ->
                if (info.status) {
                    AlertDialog.Builder(this)
                        .setTitle("Surat Jalan Selesai")
                        .setMessage("Surat jalan ini sudah diselesaikan, pengiriman lokasi tidak dapat dilakukan.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                    return@observe
                }

                info.noTravelDocument?.let { noDoc ->
                    val alamat = info.sendTo ?: ""

                    val suratJalanChip = createChip(noDoc) { chip ->
                        val index = binding.chipGroupSuratJalan.indexOfChild(chip)
                        if (index != -1) {
                            binding.chipGroupSuratJalan.removeViewAt(index)
                            binding.chipGroupAlamatPengiriman.removeViewAt(index)
                            generalViewModel.removeTravelDocument(noDoc)
                        }
                    }

                    val alamatChip = createChip(alamat) { chip ->
                        val index = binding.chipGroupAlamatPengiriman.indexOfChild(chip)
                        if (index != -1) {
                            binding.chipGroupAlamatPengiriman.removeViewAt(index)
                            binding.chipGroupSuratJalan.removeViewAt(index)
                            generalViewModel.removeTravelDocument(noDoc)
                        }
                    }

                    binding.chipGroupSuratJalan.addView(suratJalanChip)
                    binding.chipGroupAlamatPengiriman.addView(alamatChip)
                }
            }
        }
    }

    private fun createChip(text: String, onClose: (Chip) -> Unit): Chip {
        return Chip(this).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                if (!isTracking) {
                    onClose(this)
                } else {
                    Toast.makeText(context, "Penghapusan tidak diizinkan saat tracking dimulai", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

//        fusedLocationClient.lastLocation
//            .addOnSuccessListener(this) { location ->
//                if (location != null) {
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//
//                    Log.d("TrackingActivity", "Latitude: $latitude, Longitude: $longitude")
//                    Toast.makeText(this, "Lokasi ditemukan: Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
//
//                    val travelDocumentIds = generalViewModel.travelDocumentInfoList.value
//                        ?.mapNotNull { it.id } ?: emptyList()
//
//                    generalViewModel.sendCurrentLocation(travelDocumentIds, latitude, longitude)
//                    generalViewModel.sendLocationResponse.observe(this) {status ->
//                        status?.let {
//                            if (it.isNotEmpty()) {
//                                val latestStatus = it.last()?.status
//                                updateStatusTextView(latestStatus)
//                            }
//                        }
//                    }
//                } else {
//                    Log.d("TrackingActivity", "Lokasi tidak ditemukan")
//                    Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
//                }
//            }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                Log.d("TrackingActivity", "Latitude: $latitude, Longitude: $longitude")
                Toast.makeText(this, "Lokasi ditemukan: Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()

                val travelDocumentIds = generalViewModel.travelDocumentInfoList.value
                    ?.mapNotNull { it.id } ?: emptyList()

                generalViewModel.sendCurrentLocation(travelDocumentIds, latitude, longitude)
                generalViewModel.sendLocationResponse.observe(this) { status ->
                    status?.let {
                        if (it.isNotEmpty()) {
                            val latestStatus = it.last()?.status
                            updateStatusTextView(latestStatus)
                        }
                    }
                }
            } else {
                Log.d("TrackingActivity", "Lokasi tidak ditemukan")
                Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatusTextView(status: String?) {
        binding.tvStatus.text = status
    }


}