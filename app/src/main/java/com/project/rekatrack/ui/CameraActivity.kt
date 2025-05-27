package com.project.rekatrack.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.TorchState
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.project.rekatrack.R
import com.project.rekatrack.databinding.ActivityCameraBinding
import com.project.rekatrack.support.TokenHandler

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var tokenHandler: TokenHandler
    private lateinit var cameraController: LifecycleCameraController
    private lateinit var flashLight: ImageView
    private var isFlashOn: Boolean = false
    private var firstCall = true
    private var scanFailedToastShown = false
    private var scanFailHandler = android.os.Handler()
    private var scanFailRunnable: Runnable? = null
    private val SCAN_FAIL_DELAY = 60_000L // 1 menit dalam milidetik

    companion object {
        private const val TAG = "CameraActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        tokenHandler = TokenHandler(this)
        if (tokenHandler.getToken().isNullOrEmpty()) {
            Log.d(TAG, "Token kosong, arahkan ke LoginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        cameraController = LifecycleCameraController(this)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        flashLight = binding.flashlight
        flashLight.setOnClickListener {
            toggleTorch()
        }
    }

    private fun toggleTorch() {
        try {
            if (cameraController.torchState.value == TorchState.ON) {
                cameraController.enableTorch(false)
            } else {
                cameraController.enableTorch(true)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume dipanggil")
        hideSystemUI()
        startCamera()
//        navigateToTracker("2")
    }

    private fun startCamera() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        val analyzer = MlKitAnalyzer(
            listOf(barcodeScanner),
            COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(this)
        ) { result: MlKitAnalyzer.Result? ->
            showResult(result)
        }

        //setup camera controller
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            analyzer
        )
        cameraController.bindToLifecycle(this)
        binding.viewFinder.controller = cameraController

        //setup torch aka flashlight
        cameraController.torchState.observe(this) { state ->
            isFlashOn = (state == TorchState.ON)
            flashLight.setImageResource(
                if (isFlashOn) R.drawable.flash_on else R.drawable.flash_off
            )
        }
    }

    private fun showResult(result: MlKitAnalyzer.Result?) {
        val overlay = binding.overlayQr
        val location = IntArray(2)
        overlay.getLocationOnScreen(location)

        val overlayWidth = overlay.width
        val overlayHeight = overlay.height
        val overlayX = location[0]
        val overlayY = location[1]

        val barcodeResults = result?.getValue(barcodeScanner)

        if (firstCall) {
            if (barcodeResults.isNullOrEmpty()) {
                // QR tidak ditemukan sama sekali → start timer kalau belum berjalan
                if (scanFailRunnable == null) {
                    startScanFailTimer()
                }
                return
            }

            // Jika ada barcode, coba cek satu-satu apakah ada yang dalam overlay
            var barcodeInOverlayFound = false
            for (barcode in barcodeResults) {
                if (isBarcodeInOverlay(barcode, overlayX, overlayY, overlayWidth, overlayHeight)) {
                    barcodeInOverlayFound = true
                    firstCall = false

                    val barcodeValue = barcode?.rawValue
                    var invalidQrToastShown = false
                    Log.d("CameraActivity", barcodeValue!!)
                    if (barcodeValue != null && barcodeValue.startsWith("SJNID:")) {
                        MaterialAlertDialogBuilder(this)
                            .setTitle("SCAN DOKUMEN SUKSES!")
                            .setPositiveButton("Lanjutkan") { dialog, which ->
                                navigateToTracker(extractId(barcodeValue))
                            }
                            .setNegativeButton("Scan Ulang") { _,_ ->
                                firstCall = true
                            }
                            .show()
                    } else {
                        if (!invalidQrToastShown) {
                            invalidQrToastShown = true

                            Toast.makeText(
                                this,
                                "Kode QR tidak valid. Silakan coba lagi.",
                                Toast.LENGTH_SHORT
                            ).show()

                            firstCall = true

                            Handler(Looper.getMainLooper()).postDelayed({
                                invalidQrToastShown = false
                            }, 5000)
                        }
                    }

//                    val barcodeValue = barcode?.rawValue ?: "Tidak diketahui"
//
//                    // Batalkan timer gagal jika berjalan
//                    cancelScanFailTimer()
//
//                    // Tampilkan dialog sukses
//                    MaterialAlertDialogBuilder(this)
//                        .setTitle("Scan QR Sukses!")
//                        .setMessage("QR Value: $barcodeValue")
//                        .setPositiveButton("Tutup") { dialog, _ ->
//                            dialog.dismiss()
//                            navigateToTracker(barcodeValue)
//                        }
//                        .show()
//
//                    break
                }
            }

//            if (!barcodeInOverlayFound) {
//                // QR ditemukan tapi tidak dalam area overlay → langsung tampilkan pesan gagal
//                if (!scanFailedToastShown) {
//                    scanFailedToastShown = true
//                    MaterialAlertDialogBuilder(this)
//                        .setTitle("Scan Gagal")
//                        .setMessage("QR tidak terdeteksi dalam area yang ditentukan.")
//                        .setPositiveButton("Scan Lagi") { dialog, _ ->
//                            firstCall = true
//                            scanFailedToastShown = false
//                            dialog.dismiss()
//                        }
//                        .show()
//                }
//            }

            if (!barcodeInOverlayFound) {
                if (!scanFailedToastShown) {
                    scanFailedToastShown = true
                    Toast.makeText(
                        this,
                        "QR tidak terdeteksi dalam area yang ditentukan.",
                        Toast.LENGTH_SHORT
                    ).show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        scanFailedToastShown = false
                    }, 5000)
                }
            }
        }
    }

    private fun extractId(barcodeValue: String): String {
        val match = Regex("SJNID:(\\d+)").find(barcodeValue)
        return match?.value?.substringAfter("SJNID:") ?: ""
    }

    private fun startScanFailTimer() {
        scanFailRunnable = Runnable {
            if (firstCall) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Scan Gagal")
                    .setMessage("Tidak ada QR code yang terdeteksi dalam waktu 1 menit.")
                    .setPositiveButton("Coba Lagi") { dialog, _ ->
                        firstCall = true
                        scanFailedToastShown = false
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        scanFailHandler.postDelayed(scanFailRunnable!!, SCAN_FAIL_DELAY)
    }

    private fun cancelScanFailTimer() {
        scanFailRunnable?.let {
            scanFailHandler.removeCallbacks(it)
            scanFailRunnable = null
        }
    }

    private fun navigateToTracker(barcodeValue: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("RESULT", barcodeValue)
        setResult(RESULT_OK, resultIntent)
        finish()
    }


    private fun isBarcodeInOverlay(barcode: Barcode?, overlayX: Int, overlayY: Int, overlayWidth: Int, overlayHeight: Int): Boolean {
        val bounds = barcode?.boundingBox
        return bounds?.let {
            it.left >= overlayX && it.right <= (overlayX + overlayWidth) &&
                    it.top >= overlayY && it.bottom <= (overlayY + overlayHeight)
        } ?: false
    }

    private fun hideSystemUI() {
        Log.d(TAG, "hideSystemUI() dipanggil")
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
}
