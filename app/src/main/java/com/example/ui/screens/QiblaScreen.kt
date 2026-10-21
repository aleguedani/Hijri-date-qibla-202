package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.CityLocation
import com.example.sensor.CompassSensorManager
import com.example.ui.components.CalibrationDialog
import com.example.ui.components.CityPickerDialog
import com.example.ui.components.IslamicHeader
import com.example.ui.components.QiblaCompassView
import com.example.util.PreferenceManager
import com.example.util.QiblaCalculator
import com.google.android.gms.location.LocationServices
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }

    // Compass sensor manager (active only on this screen)
    val sensorManager = remember { CompassSensorManager(context) }
    val heading by sensorManager.heading.collectAsState()
    val hasCompass = sensorManager.hasCompassSensor

    // Lifecycle effect to start and stop sensors strictly with screen visibility
    DisposableEffect(Unit) {
        sensorManager.start()
        onDispose {
            sensorManager.stop()
        }
    }

    // Location state: GPS vs City vs Custom
    var locationMode by remember { mutableStateOf(prefs.locationMode) }
    var locationName by remember { mutableStateOf("القاهرة، مصر") }
    var currentLatitude by remember { mutableDoubleStateOf(prefs.manualLatitude) }
    var currentLongitude by remember { mutableDoubleStateOf(prefs.manualLongitude) }

    var showCityPicker by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    fun fetchGpsLocation() {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        currentLatitude = loc.latitude
                        currentLongitude = loc.longitude
                        locationName = "موقع GPS التلقائي"
                        locationMode = "gps"
                        prefs.locationMode = "gps"
                        prefs.manualLatitude = loc.latitude
                        prefs.manualLongitude = loc.longitude
                    } else {
                        // Fallback to LocationManager
                        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                        val lastGps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (lastGps != null) {
                            currentLatitude = lastGps.latitude
                            currentLongitude = lastGps.longitude
                            locationName = "موقع GPS التلقائي"
                            locationMode = "gps"
                            prefs.locationMode = "gps"
                            prefs.manualLatitude = lastGps.latitude
                            prefs.manualLongitude = lastGps.longitude
                        }
                    }
                }
            } catch (_: Exception) {
                // Handled gracefully
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationPermissionGranted = fineGranted || coarseGranted
        if (locationPermissionGranted) {
            fetchGpsLocation()
        }
    }

    // Initialize with saved city or location
    LaunchedEffect(Unit) {
        val savedCity = CityLocation.PRESET_CITIES.find { it.id == prefs.selectedCityId }
        if (prefs.locationMode == "gps" && locationPermissionGranted) {
            fetchGpsLocation()
        } else if (savedCity != null) {
            currentLatitude = savedCity.latitude
            currentLongitude = savedCity.longitude
            locationName = "${savedCity.nameAr}، ${savedCity.countryAr}"
        }
    }

    // Real Qibla Bearing and Distance Calculation
    val qiblaBearing = remember(currentLatitude, currentLongitude) {
        QiblaCalculator.calculateQiblaBearing(currentLatitude, currentLongitude)
    }
    val distanceToKaaba = remember(currentLatitude, currentLongitude) {
        QiblaCalculator.calculateDistanceToKaabaKm(currentLatitude, currentLongitude).roundToInt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("qibla_screen")
    ) {
        IslamicHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Bar Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (locationMode == "gps") Icons.Default.MyLocation else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = locationName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "المسافة إلى الكعبة المشرفة: $distanceToKaaba كم",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // City Selection Button
                        OutlinedButton(
                            onClick = { showCityPicker = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_change_city")
                        ) {
                            Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تغيير", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    // GPS Quick Action button if not using GPS
                    if (locationMode != "gps" || !locationPermissionGranted) {
                        FilledTonalButton(
                            onClick = {
                                if (locationPermissionGranted) {
                                    fetchGpsLocation()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحديد موقعي تلقائياً عبر GPS", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Warning Banner if magnetometer is not available
            if (!hasCompass) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "هذا الجهاز لا يدعم المستشعر المغناطيسي. تم حساب زاوية القبلة بدقة (${qiblaBearing.roundToInt()}° من الشمال) ويمكنك توجيه هاتفك بحسب الشمال الجغرافي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Main Compass Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "بوصلة اتجاه القبلة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = { showCalibrationDialog = true },
                            modifier = Modifier.testTag("btn_calibrate_compass")
                        ) {
                            Icon(Icons.Default.AllInclusive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("معايرة البوصلة", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Canvas Compass View
                    QiblaCompassView(
                        deviceHeading = heading,
                        qiblaBearing = qiblaBearing,
                        hasCompassSensor = hasCompass
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialogs
    if (showCityPicker) {
        CityPickerDialog(
            selectedCityId = prefs.selectedCityId,
            onCitySelected = { city ->
                prefs.selectedCityId = city.id
                prefs.locationMode = "city"
                prefs.manualLatitude = city.latitude
                prefs.manualLongitude = city.longitude
                currentLatitude = city.latitude
                currentLongitude = city.longitude
                locationName = "${city.nameAr}، ${city.countryAr}"
                locationMode = "city"
            },
            onCustomCoordinates = { lat, lng, name ->
                prefs.locationMode = "manual"
                prefs.manualLatitude = lat
                prefs.manualLongitude = lng
                currentLatitude = lat
                currentLongitude = lng
                locationName = name
                locationMode = "manual"
            },
            onDismiss = { showCityPicker = false }
        )
    }

    if (showCalibrationDialog) {
        CalibrationDialog(
            onDismiss = { showCalibrationDialog = false }
        )
    }
}
