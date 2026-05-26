package com.example.ui.screens.qibla

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _hasSensors = MutableStateFlow(true)
    val hasSensors: StateFlow<Boolean> = _hasSensors.asStateFlow()

    init {
        val available = rotationSensor != null || (accelerometer != null && magnetometer != null)
        _hasSensors.value = available
    }

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _qiblaDirection = MutableStateFlow(292f) // Default for Dhaka
    val qiblaDirection: StateFlow<Float> = _qiblaDirection.asStateFlow()

    private val _distance = MutableStateFlow(5874) // Default for Dhaka
    val distance: StateFlow<Int> = _distance.asStateFlow()

    private val _locationName = MutableStateFlow("ঢাকা, বাংলাদেশ (ডিফল্ট)")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _hasLocation = MutableStateFlow(false)
    val hasLocation: StateFlow<Boolean> = _hasLocation.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // Mecca coordinates
    private val meccaLat = 21.422487
    private val meccaLng = 39.826206

    fun startSensors() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    calculateQibla(location.latitude, location.longitude)
                    _locationName.value = "বর্তমান অবস্থান"
                    _hasLocation.value = true
                } else {
                    _locationName.value = "ঢাকা, বাংলাদেশ (ডিফল্ট)"
                    _hasLocation.value = false
                }
            }.addOnFailureListener {
                _locationName.value = "ঢাকা, বাংলাদেশ (ডিফল্ট)"
                _hasLocation.value = false
            }
        } catch (e: Exception) {
            _locationName.value = "ঢাকা, বাংলাদেশ (ডিফল্ট)"
            _hasLocation.value = false
        }
    }

    private fun calculateQibla(lat: Double, lng: Double) {
        val lat1 = Math.toRadians(lat)
        val lat2 = Math.toRadians(meccaLat)
        val dLng = Math.toRadians(meccaLng - lng)

        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)

        var qibla = Math.toDegrees(atan2(y, x)).toFloat()
        if (qibla < 0) qibla += 360f

        _qiblaDirection.value = qibla

        // approximate distance
        val results = FloatArray(1)
        Location.distanceBetween(lat, lng, meccaLat, meccaLng, results)
        _distance.value = (results[0] / 1000).toInt()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (az < 0) az += 360f
            _azimuth.value = az
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values
        }

        if (gravity != null && geomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (az < 0) az += 360f
                _azimuth.value = az
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
