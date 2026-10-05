package com.igrupos.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.igrupos.domain.model.EntityStatus

val StatusGreen = Color(0xFF4CAF50)
val StatusYellow = Color(0xFFFFC107)
val StatusRed = Color(0xFFF44336)
val StatusGray = Color(0xFF9E9E9E)

fun EntityStatus.toColor(): Color = when (this) {
    EntityStatus.GREEN -> StatusGreen
    EntityStatus.YELLOW -> StatusYellow
    EntityStatus.RED -> StatusRed
    EntityStatus.GRAY -> StatusGray
}

@Composable
fun StatusIndicator(
    status: EntityStatus,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(status.toColor())
    )
}
