package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KeyActionDark
import com.example.ui.theme.KeyClear
import com.example.ui.theme.KeyEquals
import com.example.ui.theme.KeyNumberDark
import com.example.ui.theme.KeyOperator
import com.example.ui.theme.KeyScientific

@Composable
fun CalculatorKeypad(
    isScientificOpen: Boolean,
    onDigitClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onFunctionClick: (String) -> Unit,
    onParenthesisClick: () -> Unit,
    onPercentageClick: () -> Unit,
    onToggleSignClick: () -> Unit,
    onDecimalClick: () -> Unit,
    onClearClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    onEqualsClick: () -> Unit,
    onToggleScientific: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val triggerHaptic = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expandable Scientific Keypad
        AnimatedVisibility(
            visible = isScientificOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sci Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SciButton(label = "sin", onClick = { triggerHaptic(); onFunctionClick("sin") }, modifier = Modifier.weight(1f))
                    SciButton(label = "cos", onClick = { triggerHaptic(); onFunctionClick("cos") }, modifier = Modifier.weight(1f))
                    SciButton(label = "tan", onClick = { triggerHaptic(); onFunctionClick("tan") }, modifier = Modifier.weight(1f))
                    SciButton(label = "ln", onClick = { triggerHaptic(); onFunctionClick("ln") }, modifier = Modifier.weight(1f))
                    SciButton(label = "log", onClick = { triggerHaptic(); onFunctionClick("log") }, modifier = Modifier.weight(1f))
                }
                // Sci Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SciButton(label = "√", onClick = { triggerHaptic(); onFunctionClick("√") }, modifier = Modifier.weight(1f))
                    SciButton(label = "x²", onClick = { triggerHaptic(); onFunctionClick("x²") }, modifier = Modifier.weight(1f))
                    SciButton(label = "xʸ", onClick = { triggerHaptic(); onFunctionClick("x^y") }, modifier = Modifier.weight(1f))
                    SciButton(label = "x!", onClick = { triggerHaptic(); onFunctionClick("!") }, modifier = Modifier.weight(1f))
                    SciButton(label = "π", onClick = { triggerHaptic(); onFunctionClick("π") }, modifier = Modifier.weight(1f))
                }
                // Sci Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SciButton(label = "e", onClick = { triggerHaptic(); onFunctionClick("e") }, modifier = Modifier.weight(1f))
                    SciButton(label = "( )", onClick = { triggerHaptic(); onParenthesisClick() }, modifier = Modifier.weight(1f))
                    SciButton(label = "1/x", onClick = { triggerHaptic(); onFunctionClick("1/") }, modifier = Modifier.weight(1f))
                    SciButton(label = "|x|", onClick = { triggerHaptic(); onFunctionClick("abs(") }, modifier = Modifier.weight(1f))
                    SciButton(label = "eˣ", onClick = { triggerHaptic(); onFunctionClick("exp(") }, modifier = Modifier.weight(1f))
                }
            }
        }

        // Row 1: Function Bar (AC, ( ), %, ÷)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                text = "AC",
                textColor = KeyClear,
                backgroundColor = KeyActionDark,
                onClick = { triggerHaptic(); onClearClick() },
                modifier = Modifier.weight(1f),
                testTag = "key_ac"
            )
            CalcKey(
                text = "( )",
                textColor = MaterialTheme.colorScheme.onSurface,
                backgroundColor = KeyActionDark,
                onClick = { triggerHaptic(); onParenthesisClick() },
                modifier = Modifier.weight(1f),
                testTag = "key_paren"
            )
            CalcKey(
                text = "%",
                textColor = MaterialTheme.colorScheme.onSurface,
                backgroundColor = KeyActionDark,
                onClick = { triggerHaptic(); onPercentageClick() },
                modifier = Modifier.weight(1f),
                testTag = "key_percent"
            )
            CalcKey(
                text = "÷",
                textColor = Color(0xFF1E1B18),
                backgroundColor = KeyOperator,
                onClick = { triggerHaptic(); onOperatorClick("÷") },
                modifier = Modifier.weight(1f),
                testTag = "key_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                text = "7",
                onClick = { triggerHaptic(); onDigitClick("7") },
                modifier = Modifier.weight(1f),
                testTag = "key_7"
            )
            CalcKey(
                text = "8",
                onClick = { triggerHaptic(); onDigitClick("8") },
                modifier = Modifier.weight(1f),
                testTag = "key_8"
            )
            CalcKey(
                text = "9",
                onClick = { triggerHaptic(); onDigitClick("9") },
                modifier = Modifier.weight(1f),
                testTag = "key_9"
            )
            CalcKey(
                text = "×",
                textColor = Color(0xFF1E1B18),
                backgroundColor = KeyOperator,
                onClick = { triggerHaptic(); onOperatorClick("×") },
                modifier = Modifier.weight(1f),
                testTag = "key_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                text = "4",
                onClick = { triggerHaptic(); onDigitClick("4") },
                modifier = Modifier.weight(1f),
                testTag = "key_4"
            )
            CalcKey(
                text = "5",
                onClick = { triggerHaptic(); onDigitClick("5") },
                modifier = Modifier.weight(1f),
                testTag = "key_5"
            )
            CalcKey(
                text = "6",
                onClick = { triggerHaptic(); onDigitClick("6") },
                modifier = Modifier.weight(1f),
                testTag = "key_6"
            )
            CalcKey(
                text = "−",
                textColor = Color(0xFF1E1B18),
                backgroundColor = KeyOperator,
                onClick = { triggerHaptic(); onOperatorClick("−") },
                modifier = Modifier.weight(1f),
                testTag = "key_minus"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                text = "1",
                onClick = { triggerHaptic(); onDigitClick("1") },
                modifier = Modifier.weight(1f),
                testTag = "key_1"
            )
            CalcKey(
                text = "2",
                onClick = { triggerHaptic(); onDigitClick("2") },
                modifier = Modifier.weight(1f),
                testTag = "key_2"
            )
            CalcKey(
                text = "3",
                onClick = { triggerHaptic(); onDigitClick("3") },
                modifier = Modifier.weight(1f),
                testTag = "key_3"
            )
            CalcKey(
                text = "+",
                textColor = Color(0xFF1E1B18),
                backgroundColor = KeyOperator,
                onClick = { triggerHaptic(); onOperatorClick("+") },
                modifier = Modifier.weight(1f),
                testTag = "key_plus"
            )
        }

        // Row 5: Sci Toggle / ±, 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcIconKey(
                icon = if (isScientificOpen) Icons.Default.Calculate else Icons.Default.Functions,
                contentDescription = if (isScientificOpen) "โหมดธรรมดา" else "โหมดวิทยาศาสตร์",
                backgroundColor = if (isScientificOpen) KeyScientific else KeyActionDark,
                iconTint = if (isScientificOpen) Color.White else MaterialTheme.colorScheme.onSurface,
                onClick = { triggerHaptic(); onToggleScientific() },
                modifier = Modifier.weight(1f),
                testTag = "key_sci_toggle"
            )
            CalcKey(
                text = "0",
                onClick = { triggerHaptic(); onDigitClick("0") },
                modifier = Modifier.weight(1f),
                testTag = "key_0"
            )
            CalcKey(
                text = ".",
                onClick = { triggerHaptic(); onDecimalClick() },
                modifier = Modifier.weight(1f),
                testTag = "key_dot"
            )
            CalcKey(
                text = "=",
                textColor = Color(0xFF1E1B18),
                backgroundColor = KeyEquals,
                onClick = { triggerHaptic(); onEqualsClick() },
                modifier = Modifier.weight(1f),
                testTag = "key_equals"
            )
        }

        // Sub Row: Backspace & Sign Toggle bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                text = "±",
                textColor = MaterialTheme.colorScheme.onSurface,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { triggerHaptic(); onToggleSignClick() },
                modifier = Modifier.weight(1f),
                height = 48,
                testTag = "key_plus_minus"
            )
            CalcIconKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "ลบตัวล่าสุด",
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                iconTint = MaterialTheme.colorScheme.onSurface,
                onClick = { triggerHaptic(); onBackspaceClick() },
                modifier = Modifier.weight(1f),
                height = 48,
                testTag = "key_backspace"
            )
        }
    }
}

@Composable
private fun CalcKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 68,
    backgroundColor: Color = KeyNumberDark,
    textColor: Color = Color.White,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(height.dp)
            .testTag(testTag),
        color = backgroundColor,
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = if (text.length > 2) 20.sp else 26.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CalcIconKey(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 68,
    backgroundColor: Color = KeyActionDark,
    iconTint: Color = Color.White,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(height.dp)
            .testTag(testTag),
        color = backgroundColor,
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint
            )
        }
    }
}

@Composable
private fun SciButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
