package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

@Composable
fun DateInputField(
    day: Int,
    month: Int,
    year: Int,
    onDateChanged: (day: Int, month: Int, year: Int) -> Unit,
    onConvertClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dayText by remember(day) { mutableStateOf(day.toString()) }
    var monthText by remember(month) { mutableStateOf(month.toString()) }
    var yearText by remember(year) { mutableStateOf(year.toString()) }

    fun updateAndNotify(newDay: Int, newMonth: Int, newYear: Int) {
        val safeYear = newYear.coerceIn(1900, 2100)
        val safeMonth = newMonth.coerceIn(1, 12)
        val maxDays = when (safeMonth) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (safeYear % 4 == 0 && (safeYear % 100 != 0 || safeYear % 400 == 0)) 29 else 28
            else -> 31
        }
        val safeDay = newDay.coerceIn(1, maxDays)

        dayText = safeDay.toString()
        monthText = safeMonth.toString()
        yearText = safeYear.toString()
        onDateChanged(safeDay, safeMonth, safeYear)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_input_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "إدخال التاريخ الميلادي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Calendar picker button
                OutlinedButton(
                    onClick = {
                        val currentCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month - 1)
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                updateAndNotify(d, m + 1, y)
                            },
                            currentCal.get(Calendar.YEAR),
                            currentCal.get(Calendar.MONTH),
                            currentCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("open_calendar_picker")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "التقويم",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("التقويم", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Quick Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {
                        val cal = Calendar.getInstance()
                        updateAndNotify(
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                        )
                    },
                    label = { Text("اليوم", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f).testTag("quick_today")
                )

                FilterChip(
                    selected = false,
                    onClick = {
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                        updateAndNotify(
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                        )
                    },
                    label = { Text("أمس", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f).testTag("quick_yesterday")
                )

                FilterChip(
                    selected = false,
                    onClick = {
                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                        updateAndNotify(
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                        )
                    },
                    label = { Text("غداً", style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f).testTag("quick_tomorrow")
                )
            }

            // Three Input Fields: Day, Month, Year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Day Input
                OutlinedTextField(
                    value = dayText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        dayText = clean
                        val parsed = clean.toIntOrNull()
                        if (parsed != null && parsed in 1..31) {
                            onDateChanged(parsed, month, year)
                        }
                    },
                    label = { Text("اليوم", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_day")
                )

                // Month Input
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        monthText = clean
                        val parsed = clean.toIntOrNull()
                        if (parsed != null && parsed in 1..12) {
                            onDateChanged(day, parsed, year)
                        }
                    },
                    label = { Text("الشهر", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_month")
                )

                // Year Input
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }
                        yearText = clean
                        val parsed = clean.toIntOrNull()
                        if (parsed != null && parsed in 1000..3000) {
                            onDateChanged(day, month, parsed)
                        }
                    },
                    label = { Text("السنة", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("input_year")
                )
            }

            // Primary Convert Button
            Button(
                onClick = onConvertClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_convert_hijri"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChangeCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تحويل إلى التاريخ الهجري",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
