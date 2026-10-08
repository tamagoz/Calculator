package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorUiState

@Composable
fun CalculatorDisplay(
    state: CalculatorUiState,
    onToggleDegreeRad: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Auto-scroll to end of expression when typing
    LaunchedEffect(state.expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calculator_display_surface"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Mode Chips Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onToggleDegreeRad,
                    label = {
                        Text(
                            text = if (state.isDegreeMode) "DEG (องศา)" else "RAD (เรเดียน)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.isDegreeMode)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("chip_deg_rad")
                )

                if (state.expression.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val toCopy = if (state.previewResult.isNotEmpty()) {
                                state.previewResult.removePrefix("= ")
                            } else {
                                state.expression
                            }
                            copyToClipboard(context, toCopy)
                        },
                        modifier = Modifier.testTag("button_copy_expression")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "คัดลอกผลลัพธ์",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Expression Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = state.expression.ifEmpty { "0" },
                    fontSize = when {
                        state.expression.length > 18 -> 32.sp
                        state.expression.length > 10 -> 40.sp
                        else -> 52.sp
                    },
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("expression_text")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Live Preview or Error Banner
            if (state.errorMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "ข้อผิดพลาด",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = state.errorMessage,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("error_text")
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = state.previewResult.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = state.previewResult,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .testTag("preview_result_text")
                            .clickable {
                                copyToClipboard(context, state.previewResult.removePrefix("= "))
                            }
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Calculator Result", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "คัดลอก: $text", Toast.LENGTH_SHORT).show()
}
