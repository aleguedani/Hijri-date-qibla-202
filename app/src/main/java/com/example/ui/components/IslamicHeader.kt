package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.HijriDate
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicGoldSecondary
import com.example.util.HijriCalendarHelper
import com.example.util.PreferenceManager

@Composable
fun IslamicHeader(
    currentHijriDate: HijriDate? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val date = currentHijriDate ?: remember(prefs.adjustmentDays) {
        HijriCalendarHelper.getTodayHijri(prefs.adjustmentDays)
    }
    val countdownText = remember(date) {
        HijriCalendarHelper.getHeaderCountdown(date)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .testTag("islamic_header_box")
    ) {
        // Banner background image
        Image(
            painter = painterResource(id = R.drawable.img_kaaba_banner),
            contentDescription = "الكعبة المشرفة",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast & spiritual glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x990A2E1D),
                            Color(0xE6061F13)
                        )
                    )
                )
        )

        // Title and countdown content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Replaced Bismillah with the countdown to Ramadan (< 3 months) or Eid al-Adha (after Ramadan)
            if (!countdownText.isNullOrEmpty()) {
                Text(
                    text = countdownText,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = IslamicGoldSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("header_countdown_text")
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "التاريخ الهجري والقبلة",
                fontFamily = AmiriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "تقويم أم القرى الموثق • اتجاه القبلة نحو الكعبة المشرفة",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFE0E0E0),
                textAlign = TextAlign.Center
            )
        }
    }
}
