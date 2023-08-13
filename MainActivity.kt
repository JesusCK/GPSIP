package com.gpsjq.gpsip

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var textViewLatitude: TextView
    private lateinit var textViewLongitude: TextView
    private lateinit var btnSendTCP: Button
    private lateinit var btnSendUDP: Button
    private lateinit var textViewLocation: TextView
    private val ipTCP1 = "181.134.169.139" // Dirección IP pública para TCP 1
    private val ipTCP2 = "181.134.169.139"// Dirección IP pública para TCP 2
    private val ipUDP1 = "181.134.169.139" // Dirección IP pública para UDP 1
    private val ipUDP2 = "181.134.169.139" // Dirección IP pública para UDP 2
    private val port = 15000 // Puerto para ambas conexiones

    private val locationBuffer = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        textViewLatitude = findViewById(R.id.textViewLatitude)
        textViewLongitude = findViewById(R.id.textViewLongitude)
        btnSendTCP = findViewById(R.id.btnSendTCP)
        btnSendUDP = findViewById(R.id.btnSendUDP)
        textViewLocation= findViewById(R.id.textView3)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val timestampSystem = System.currentTimeMillis()
                val timestampSatellite = location.elapsedRealtimeNanos / 1000000
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getDefault()

                val formattedTimeSystem = dateFormat.format(Date(timestampSystem))
                val formattedTimeSatellite = dateFormat.format(Date(timestampSatellite))

                textViewLocation.text = "Tiempo Sistema: $formattedTimeSystem"
                textViewLatitude.text = "Latitud: ${location.latitude}"
                textViewLongitude.text = "Longitud: ${location.longitude}"
                locationBuffer.add("LAT:${location.latitude},LONG:${location.longitude}")
            }
        }

        btnSendTCP.setOnClickListener {
            sendLocationTCP()
        }

        btnSendUDP.setOnClickListener {
            sendLocationUDP()
        }
    }

    private fun sendLocationTCP() {
        if (locationBuffer.isNotEmpty()) {
            val location = locationBuffer.last()
            Thread {
                try {
                    val socket1 = Socket(ipTCP1, port)
                    val outputStream1 = socket1.getOutputStream()
                    outputStream1.write(location.toByteArray())
                    outputStream1.close()
                    socket1.close()

                    val socket2 = Socket(ipTCP2, port)
                    val outputStream2 = socket2.getOutputStream()
                    outputStream2.write(location.toByteArray())
                    outputStream2.close()
                    socket2.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun sendLocationUDP() {
        if (locationBuffer.isNotEmpty()) {
            val location = locationBuffer.last()
            Thread {
                try {
                    val sendData = location.toByteArray()

                    val address1 = InetAddress.getByName(ipUDP1)
                    val socket1 = DatagramSocket()
                    val packet1 = DatagramPacket(sendData, sendData.size, address1, port)
                    socket1.send(packet1)
                    socket1.close()

                    val address2 = InetAddress.getByName(ipUDP2)
                    val socket2 = DatagramSocket()
                    val packet2 = DatagramPacket(sendData, sendData.size, address2, port)
                    socket2.send(packet2)
                    socket2.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}
