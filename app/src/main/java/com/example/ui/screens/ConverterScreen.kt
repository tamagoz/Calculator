package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.UnitCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val converterState by viewModel.converterState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val units = viewModel.getUnitsForCategory(converterState.category)

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "แปลงหน่วยการวัด",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Category Chips (Horizontal Scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitCategory.values().forEach { category ->
                FilterChip(
                    selected = converterState.category == category,
                    onClick = { viewModel.setUnitCategory(category) },
                    label = { Text(category.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Input Value Field
        OutlinedTextField(
            value = converterState.inputValue,
            onValueChange = {
                val filtered = it.filter { c -> c.isDigit() || c == '.' || c == '-' }
                viewModel.setConverterInput(filtered)
            },
            label = { Text("ค่าที่ต้องการแปลง") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("converter_input"),
            shape = RoundedCornerShape(16.dp)
        )

        // From Unit Dropdown
        ExposedDropdownMenuBox(
            expanded = fromExpanded,
            onExpandedChange = { fromExpanded = !fromExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val fromUnitName = units.getOrNull(converterState.fromUnitIndex)?.name ?: ""
            OutlinedTextField(
                value = fromUnitName,
                onValueChange = {},
                readOnly = true,
                label = { Text("จากหน่วย (From)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = fromExpanded,
                onDismissRequest = { fromExpanded = false }
            ) {
                units.forEachIndexed { index, unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            viewModel.setFromUnitIndex(index)
                            fromExpanded = false
                        }
                    )
                }
            }
        }

        // Swap Units Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FilledIconButton(
                onClick = { viewModel.swapUnits() },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "สลับหน่วย",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // To Unit Dropdown
        ExposedDropdownMenuBox(
            expanded = toExpanded,
            onExpandedChange = { toExpanded = !toExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            val toUnitName = units.getOrNull(converterState.toUnitIndex)?.name ?: ""
            OutlinedTextField(
                value = toUnitName,
                onValueChange = {},
                readOnly = true,
                label = { Text("เป็นหน่วย (To)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp)
            )
            ExposedDropdownMenu(
                expanded = toExpanded,
                onDismissRequest = { toExpanded = false }
            ) {
                units.forEachIndexed { index, unit ->
                    DropdownMenuItem(
                        text = { Text(unit.name) },
                        onClick = {
                            viewModel.setToUnitIndex(index)
                            toExpanded = false
                        }
                    )
                }
            }
        }

        // Converted Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ผลการแปลงหน่วย",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Converted Result", converterState.resultValue)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "คัดลอก: ${converterState.resultValue}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "คัดลอกผลลัพธ์",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = converterState.resultValue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = units.getOrNull(converterState.toUnitIndex)?.name ?: "",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Thai Land Unit Explainer (Special feature for Area category)
        if (converterState.category == UnitCategory.AREA) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "สูตรมาตราวัดพื้นที่ดินไทย:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("• 1 ไร่ = 4 งาน = 400 ตารางวา = 1,600 ตร.ม.", fontSize = 13.sp)
                    Text("• 1 งาน = 100 ตารางวา = 400 ตร.ม.", fontSize = 13.sp)
                    Text("• 1 ตารางวา = 4 ตร.ม.", fontSize = 13.sp)
                }
            }
        }

        // Pressure Unit Explainer
        if (converterState.category == UnitCategory.PRESSURE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "มาตราวัดแรงดันที่ใช้บ่อย:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("• 1 bar ≈ 14.50 psi ≈ 1.02 kg/cm² = 100 kPa", fontSize = 13.sp)
                    Text("• 1 atm = 101.325 kPa = 760 mmHg = 14.696 psi", fontSize = 13.sp)
                    Text("• ลมยางรถยนต์มาตรฐาน: 30 - 35 psi (≈ 2.07 - 2.41 bar)", fontSize = 13.sp)
                }
            }
        }
    }
}
