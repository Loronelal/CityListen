package com.citylisten.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.citylisten.ui.theme.CityListenTheme
import com.citylisten.ui.theme.OfflineBadgeBgDark
import com.citylisten.ui.theme.OfflineBadgeBgLight
import com.citylisten.ui.theme.OfflineBadgeTextDark
import com.citylisten.ui.theme.OfflineBadgeTextLight

@Composable
fun OfflineBadge(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) OfflineBadgeBgDark else OfflineBadgeBgLight
    val textColor = if (isDark) OfflineBadgeTextDark else OfflineBadgeTextLight

    Text(
        text = "Офлайн",
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Preview(name = "OfflineBadge · Light", showBackground = true)
@Composable
fun OfflineBadgePreview() {
    CityListenTheme { OfflineBadge() }
}