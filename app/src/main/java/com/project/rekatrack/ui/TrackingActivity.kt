package com.project.rekatrack.ui

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.project.rekatrack.R
import com.project.rekatrack.databinding.ActivityTrackingBinding

class TrackingActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnAddSuratJalan.setOnClickListener {
            val dummySuratJalan = "No. 120/R2/NT/2022"
            addSuratJalanChip(dummySuratJalan)
        }
    }

    private fun addSuratJalanChip(text: String) {
        val chip = Chip(this).apply {
            this.text = text
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.chipGroupSuratJalan.removeView(this)
            }
        }
        binding.chipGroupSuratJalan.addView(chip)
    }
}