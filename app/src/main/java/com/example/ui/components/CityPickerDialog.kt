package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.CityLocation
import com.example.util.QiblaCalculator
import kotlin.math.roundToInt

@Composable
fun CityPickerDialog(
    selectedCityId: String,
    onCitySelected: (CityLocation) -> Unit,
    onCustomCoordinates: (lat: Double, lng: Double, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isManualMode by remember { mutableStateOf(false) }

    var customLatText by remember { mutableStateOf("") }
    var customLngText by remember { mutableStateOf("") }
    var customNameText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CityLocation.PRESET_CITIES
        } else {
            CityLocation.PRESET_CITIES.filter {
                it.nameAr.contains(searchQuery.trim()) || it.countryAr.contains(searchQuery.trim())
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .testTag("dialog_city_picker")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isManualMode) "إدخال إحداثيات مخصصة" else "اختيار المدينة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(
                        onClick = { isManualMode = !isManualMode }
                    ) {
                        Text(if (isManualMode) "قائمة المدن" else "إحداثيات يدوية")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isManualMode) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث عن مدينة أو دولة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_city_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("list_cities"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCities, key = { it.id }) { city ->
                            val isSelected = city.id == selectedCityId
                            val distance = QiblaCalculator.calculateDistanceToKaabaKm(city.latitude, city.longitude).roundToInt()
                            val bearing = QiblaCalculator.calculateQiblaBearing(city.latitude, city.longitude).roundToInt()

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCitySelected(city)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationCity,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = city.nameAr,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${city.countryAr} • القبلة: $bearing°",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$distance كم",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Manual Coordinate Entry
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "أدخل خط العرض وخط الطول بالدرجات العشرية:",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = customNameText,
                            onValueChange = { customNameText = it },
                            label = { Text("اسم المكان / المدينة (اختياري)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customLatText,
                            onValueChange = { customLatText = it },
                            label = { Text("خط العرض (Latitude: -90 إلى 90)") },
                            placeholder = { Text("مثال: 30.0444") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customLngText,
                            onValueChange = { customLngText = it },
                            label = { Text("خط الطول (Longitude: -180 إلى 180)") },
                            placeholder = { Text("مثال: 31.2357") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = {
                                val lat = customLatText.toDoubleOrNull()
                                val lng = customLngText.toDoubleOrNull()
                                if (lat == null || lat !in -90.0..90.0) {
                                    errorMessage = "يرجى إدخال خط عرض صحيح بين -90 و 90"
                                    return@Button
                                }
                                if (lng == null || lng !in -180.0..180.0) {
                                    errorMessage = "يرجى إدخال خط طول صحيح بين -180 و 180"
                                    return@Button
                                }
                                val name = if (customNameText.isNotBlank()) customNameText.trim() else "موقع مخصص"
                                onCustomCoordinates(lat, lng, name)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("تطبيق الإحداثيات وحساب القبلة")
                        }
                    }
                }

                // Close Button
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("إلغاء")
                }
            }
        }
    }
}
