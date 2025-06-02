package com.project.rekatrack.support

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.project.rekatrack.R
import com.project.rekatrack.data.request.SendLocationRequest
import com.project.rekatrack.network.ApiConfig
import com.project.rekatrack.ui.TrackingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackingService : Service() {
    private val CHANNEL_ID = "TrackingServiceChannel"
    private var isTracking = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var trackingJob: Job? = null
    private var documentIds: List<String> = emptyList()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isTracking = intent?.getBooleanExtra("isTracking", true) ?: true
        documentIds = intent?.getStringArrayListExtra("travelDocumentIds") ?: emptyList()

        createNotificationChannel()
        val notification = buildNotification()
        startForeground(1, notification)

        startLocationUpdates()
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, TrackingActivity::class.java).apply {
            putExtra("isTracking", isTracking)
            putStringArrayListExtra("travelDocumentIds", ArrayList(documentIds))
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Aktif")
            .setContentText("Lokasi sedang dikirim secara berkala")
            .setSmallIcon(R.drawable.ic_logo_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startLocationUpdates() {
        trackingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && isTracking) {
                getLocation()
                delay(5000)
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude

                Log.d("TrackingService", "Lokasi: ${it.latitude}, ${it.longitude}")
                sendLocationToServer(documentIds, latitude, longitude)
            }
        }
    }

    private fun sendLocationToServer(ids: List<String>, latitude: Double, longitude: Double) {
        val token = TokenHandler(this).getToken() ?: return
        val apiService = ApiConfig.getApiService(token)

        val intIds = ids.mapNotNull { it.toIntOrNull() }

        val requestBody = SendLocationRequest(
            travel_document_id = intIds,
            latitude = latitude,
            longitude = longitude
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.sendCurrentLocation(
                    sendLocationRequest = requestBody
                )
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val status = responseBody?.data?.firstOrNull()?.status
                    if (!status.isNullOrEmpty()) {
                        val intent = Intent("com.project.rekatrack.STATUS_UPDATE")
                        intent.putExtra("status", status)
                        sendBroadcast(intent)
                    }
                    Log.d("TrackingService", "Berhasil kirim lokasi: ${response.body()}")
                } else {
                    Log.e("TrackingService", "Gagal kirim lokasi: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("TrackingService", "Error kirim lokasi", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        isTracking = false
        trackingJob?.cancel()
        super.onDestroy()
    }

}