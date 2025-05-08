package com.project.rekatrack.ui

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.project.rekatrack.R
import com.project.rekatrack.data.repository.Repository
import com.project.rekatrack.data.response.TravelDocumentInfo
import com.project.rekatrack.data.viewModel.GeneralViewModel
import com.project.rekatrack.databinding.ActivityTrackingBinding
import com.project.rekatrack.network.ApiConfig
import com.project.rekatrack.support.TokenHandler
import com.project.rekatrack.viewModelFactory.ViewModelFactory

class TrackingActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTrackingBinding
    private lateinit var generalViewModel: GeneralViewModel
    private lateinit var scanLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
    }

    private fun fetchSuratJalan(scannedData: String) {
        generalViewModel.getTravelDocument(scannedData)

        generalViewModel.travelDocumentInfoList.observe(this) { travelDocumentInfoList ->
            binding.chipGroupSuratJalan.removeAllViews()
            binding.chipGroupAlamatPengiriman.removeAllViews()

            travelDocumentInfoList?.forEach { info ->
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
            setOnCloseIconClickListener { onClose(this) }
        }
    }
}