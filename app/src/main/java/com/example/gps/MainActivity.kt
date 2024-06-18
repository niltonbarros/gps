package com.example.gps

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.gps.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {//Início da classe MainActivity

    //Configuração do viewBinding
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //Variavel da localização
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Constantes de uso do app
    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1001
        const val NOTIFICATION_CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {//Início da função onCreate
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btnEnableGPS.setOnClickListener {
            enableGPS()
        }
    }//fim do override onCreate

    private fun enableGPS() {//Inicio funcao enableGPS
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            } else {
                requestLocation()
            }
        }
    }//fim função enbleGPS

    private fun requestLocation() {//Inicio Latitude Longitude
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    binding.tvLocation.text = "Latitude: $latitude, Longitude: $longitude"
                    sendNotification(latitude, longitude)
                } else {
                    binding.tvLocation.text = "Localização não encontrada."
                }
            }
    }//Fim Latitude Longitude

    //Início - Envia notificação
    private fun sendNotification(latitude: Double, longitude: Double) {
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle("Coordenadas Atuais")
            .setContentText("Latitude: $latitude, Longitude: $longitude")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }//Fim - Envia notificação

    //canal de notificação (inicio)
    private fun createNotificationChannel() {
        val channelName = "Location Channel"
        val channelDescription = "Channel for location notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance).apply {
            description = channelDescription
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }//canal de notificação (fim)

    //Início - Resultado da Permissão
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            } else {
                binding.tvLocation.text = "Permissão de localização negada."
            }
        }
    }//Fim - Resultado da Permissão
}//Fim da classe MainActivity

