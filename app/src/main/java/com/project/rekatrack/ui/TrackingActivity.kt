package com.project.rekatrack.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.ContextCompat
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
    private var hasTracking = false
    private var hasDocument = false

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

        val prefs = getSharedPreferences("tracking_prefs", MODE_PRIVATE)
        isTracking = prefs.getBoolean("isTracking", false)
        val savedIds = prefs.getStringSet("travelDocumentIds", emptySet())?.toList() ?: emptyList()
        Log.d("TrackingActivity", "SharedPrefs isTracking: $isTracking, travelDocumentIds: $savedIds")

        if (savedIds.isNotEmpty()) {
            hasTracking = true
            binding.btnStartTracker.text = if (isTracking) "Matikan Tracker" else "Hidupkan Tracker"

            generalViewModel.clearTravelDocuments()

            savedIds.forEach { id ->
                generalViewModel.getTravelDocument(id)
            }

            updateButtonStates()
        } else {
            if (intent != null) {
                val intentIsTracking = intent.getBooleanExtra("isTracking", false)
                val intentIds = intent.getStringArrayListExtra("travelDocumentIds")
                Log.d("TrackingActivity", "Intent isTracking: $intentIsTracking, travelDocumentIds: $intentIds")

                if (!intentIds.isNullOrEmpty()) {
                    isTracking = intentIsTracking
                    hasTracking = true
                    binding.btnStartTracker.text = if (isTracking) "Matikan Tracker" else "Hidupkan Tracker"

                    intentIds.forEach { id ->
                        Log.d("TrackingActivity", "Loading Travel Document from intent: $id")
                        generalViewModel.getTravelDocument(id)
                    }

                    updateButtonStates()
                    return  // **Stop di sini supaya gak load dari prefs lagi**
                }
            }
        }

        generalViewModel.travelDocumentInfoList.observe(this) { travelDocumentInfoList ->
            binding.chipGroupSuratJalan.removeAllViews()
            binding.chipGroupAlamatPengiriman.removeAllViews()

            travelDocumentInfoList?.forEach { info ->
                val suratJalanChip = createChip(info.noTravelDocument ?: "") { chip ->
                    if (!hasTracking) {
                        generalViewModel.removeTravelDocument(info.noTravelDocument ?: "")
                        updateButtonStates()
                    } else {
                        Toast.makeText(this, "Penghapusan tidak diizinkan setelah tracking dimulai", Toast.LENGTH_SHORT).show()
                    }
                }

                val alamatChip = createChip(info.sendTo ?: "") { chip ->
                    if (!hasTracking) {
                        generalViewModel.removeTravelDocument(info.noTravelDocument ?: "")
                        updateButtonStates()
                    } else {
                        Toast.makeText(this, "Penghapusan tidak diizinkan setelah tracking dimulai", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.chipGroupSuratJalan.addView(suratJalanChip)
                binding.chipGroupAlamatPengiriman.addView(alamatChip)
            }

            updateButtonStates()
        }


        generalViewModel.isDocumentAlreadySent.observe(this) { isSent ->
            if (isSent == true) {
                AlertDialog.Builder(this)
                    .setTitle("Surat Jalan Selesai")
                    .setMessage("Surat jalan ini sudah diselesaikan, pengiriman lokasi tidak dapat dilakukan.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        generalViewModel.sendLocationResponse.observe(this) {status ->
            status?.let {
                if (it.isNotEmpty()) {
                    val latestStatus = it.last()?.status
                    updateStatusTextView(latestStatus)
                }
            }
        }

        updateButtonStates()

        binding.btnAddSuratJalan.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            scanLauncher.launch(intent)
        }

        binding.btnStartTracker.isEnabled = hasDocument

        binding.btnStartTracker.setOnClickListener {
            val travelDocumentIds = generalViewModel.travelDocumentInfoList.value
                ?.mapNotNull { it.id?.toString() } ?: emptyList()

            if (!hasTracking) {
                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah data surat jalan sudah benar semua?\n\nSetelah pelacakan dimulai, Anda tidak dapat menambah atau menghapus surat jalan.")
                    .setPositiveButton("Lanjutkan") { _, _ ->
                        hasTracking = true
                        isTracking = true
                        binding.btnStartTracker.text = "Matikan Tracker"
                        startTrackingService(isTracking, travelDocumentIds)
                        saveTrackingState(isTracking, travelDocumentIds)
                        updateButtonStates()
                    }
                    .setNegativeButton("Batal") { _, _ -> }
                    .show()
            } else {
                isTracking = !isTracking
                if (isTracking) {
                    binding.btnStartTracker.text = "Matikan Tracker"
                    startTrackingService(isTracking, travelDocumentIds)
                    saveTrackingState(isTracking, travelDocumentIds)
                } else {
                    binding.btnStartTracker.text = "Hidupkan Tracker"
                    saveTrackingState(isTracking, travelDocumentIds)
                    stopTrackingService()
                    updateStatus()
                }
                updateButtonStates()
            }
        }


        binding.btnStopTracker.isEnabled = hasDocument
        binding.btnStopTracker.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konfirmasi Penyelesaian")
                .setMessage("Apakah Anda yakin ingin menyelesaikan pengiriman? Setelah ini Anda tidak dapat menambahkan surat jalan atau mengaktifkan pelacak lagi.")
                .setPositiveButton("Ya") { _, _ ->
                    binding.btnAddSuratJalan.isEnabled = false
                    binding.btnStartTracker.isEnabled = false
                    binding.btnStopTracker.isEnabled = false
                    completeTrackingActivity()
                }
                .setNegativeButton("Batal", null)
                .show()
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

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
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

                                //clear prefs
                                val prefs = getSharedPreferences("tracking_prefs", MODE_PRIVATE)
                                prefs.edit().clear().apply()

                                val intent = Intent(this, MenusActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
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

    private fun startTrackingService(isTracking: Boolean, travelDocumentIds: List<String>) {
        val serviceIntent = Intent(this, TrackingService::class.java).apply {
            putExtra("isTracking", isTracking)
            putStringArrayListExtra("travelDocumentIds", ArrayList(travelDocumentIds))
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun saveTrackingState(isTracking: Boolean, travelDocumentIds: List<String>) {
        val prefs = getSharedPreferences("tracking_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("isTracking", isTracking)
            putStringSet("travelDocumentIds", travelDocumentIds.toSet())
            apply()
        }
    }

    private fun stopTrackingService() {
        val serviceIntent = Intent(this, TrackingService::class.java)
        stopService(serviceIntent)
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra("status")
            status?.let {
                Log.d("TrackingActivity", "Menerima status update dari Service: $it")
                updateStatusTextView(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(statusReceiver, IntentFilter("com.project.rekatrack.STATUS_UPDATE"))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(statusReceiver)
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

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener(this) { location ->
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
                Toast.makeText(this, "Lokasi tidak ditemukan, pastikan gps anda menyala", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun fetchSuratJalan(scannedData: String) {
        generalViewModel.getTravelDocument(scannedData)

        generalViewModel.travelDocumentInfoList.observe(this) { travelDocumentInfoList ->
            binding.chipGroupSuratJalan.removeAllViews()
            binding.chipGroupAlamatPengiriman.removeAllViews()

            travelDocumentInfoList?.forEach { info ->

                updateButtonStates()

                info.noTravelDocument?.let { noDoc ->
                    val alamat = info.sendTo ?: ""

                    val suratJalanChip = createChip(noDoc) { chip ->
                        val index = binding.chipGroupSuratJalan.indexOfChild(chip)
                        if (index != -1) {
                            binding.chipGroupSuratJalan.removeViewAt(index)
                            binding.chipGroupAlamatPengiriman.removeViewAt(index)
                            generalViewModel.removeTravelDocument(noDoc)
                            updateButtonStates()
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
                if (!hasTracking) {
                    onClose(this)
                } else {
                    Toast.makeText(context, "Penghapusan tidak diizinkan setelah tracking dimulai", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateButtonStates() {
        val hasDocs = generalViewModel.travelDocumentInfoList.value?.isNotEmpty() == true

        binding.btnStartTracker.isEnabled = hasDocs
        binding.btnAddSuratJalan.isEnabled = !hasTracking
        binding.btnStopTracker.isEnabled = hasDocs && hasTracking

        if (hasTracking) {
            for (i in 0 until binding.chipGroupSuratJalan.childCount) {
                val chip = binding.chipGroupSuratJalan.getChildAt(i) as Chip
                chip.isCloseIconVisible = false
            }
            for (i in 0 until binding.chipGroupAlamatPengiriman.childCount) {
                val chip = binding.chipGroupAlamatPengiriman.getChildAt(i) as Chip
                chip.isCloseIconVisible = false
            }
        }
    }

    private fun updateStatusTextView(status: String?) {
        binding.tvStatus.text = status
    }


}