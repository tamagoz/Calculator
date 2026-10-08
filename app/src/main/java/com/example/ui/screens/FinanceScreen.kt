package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.ui.CalculatorViewModel
import com.example.ui.VatCalculationMode
import java.text.DecimalFormat

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val financeState by viewModel.financeState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val df = remember { DecimalFormat("#,##0.00") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("ส่วนลด & ภาษี 7%") },
                icon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                modifier = Modifier.testTag("tab_discount_vat")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("แบ่งบิล & ทิป") },
                icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                modifier = Modifier.testTag("tab_split_bill")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSubTab == 0) {
            // --- Discount & 7% VAT Calculator (รองรับทั้งถอด VAT และบวก VAT) ---
            val isExtractMode = financeState.vatMode == VatCalculationMode.EXTRACT_VAT

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // VAT Mode Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "เลือกรูปแบบการคำนวณภาษี VAT:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = isExtractMode,
                                onClick = { viewModel.setVatMode(VatCalculationMode.EXTRACT_VAT) },
                                label = { Text("ถอด VAT (ใน)") },
                                leadingIcon = {
                                    if (isExtractMode) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_vat_mode_extract"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = !isExtractMode,
                                onClick = { viewModel.setVatMode(VatCalculationMode.ADD_VAT) },
                                label = { Text("บวก VAT (นอก)") },
                                leadingIcon = {
                                    if (!isExtractMode) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_vat_mode_add"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isExtractMode)
                                "★ โหมดถอด VAT: ใส่ยอดรวมภาษีที่ต้องจ่าย → คำนวณแปลงกลับเป็นราคาสุทธิก่อนภาษี"
                            else
                                "★ โหมดบวก VAT: ใส่ราคาก่อนภาษี → คำนวณบวกภาษีมูลค่าเพิ่ม 7% เข้าไป",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Input Price Field
                OutlinedTextField(
                    value = financeState.originalPrice,
                    onValueChange = { viewModel.updateOriginalPrice(it.filter { c -> c.isDigit() || c == '.' }) },
                    label = {
                        Text(
                            if (isExtractMode) "ยอดรวมภาษีที่ต้องจ่าย (บาท)" else "ราคาสุทธิก่อนภาษี (บาท)"
                        )
                    },
                    prefix = { Text("฿ ") },
                    supportingText = {
                        Text(
                            if (isExtractMode)
                                "ใส่ยอดเงินรวมภาษีทั้งหมดที่จ่าย (เช่น 1,070 หรือ 1,000)"
                            else
                                "ใส่ราคาสินค้าหรือบริการก่อนคิดภาษี"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_original_price"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Discount Percent & Quick Chips
                Column {
                    OutlinedTextField(
                        value = financeState.discountPercent,
                        onValueChange = { viewModel.updateDiscountPercent(it.filter { c -> c.isDigit() || c == '.' }) },
                        label = { Text("ส่วนลด (ถ้ามี) (%)") },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_discount_percent"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0", "5", "10", "15", "20", "30", "50").forEach { preset ->
                            SuggestionChip(
                                onClick = { viewModel.updateDiscountPercent(preset) },
                                label = { Text(if (preset == "0") "0% (ไม่ลด)" else "$preset%") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (financeState.discountPercent == preset)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // VAT Percent
                Column {
                    OutlinedTextField(
                        value = financeState.vatPercent,
                        onValueChange = { viewModel.updateVatPercent(it.filter { c -> c.isDigit() || c == '.' }) },
                        label = { Text("อัตราภาษีมูลค่าเพิ่ม VAT (%)") },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vat_percent"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = { viewModel.updateVatPercent("7") },
                            label = { Text("VAT 7% (มาตรฐานไทย)") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (financeState.vatPercent == "7")
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        SuggestionChip(
                            onClick = { viewModel.updateVatPercent("0") },
                            label = { Text("ไม่มี VAT (0%)") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (financeState.vatPercent == "0")
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Result Summary Card
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExtractMode) "ผลการถอดภาษี VAT" else "สรุปผลการคำนวณภาษี VAT",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    val toCopy = if (isExtractMode)
                                        df.format(financeState.netBeforeVat)
                                    else
                                        df.format(financeState.finalTotal)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("VAT Calculation", toCopy)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "คัดลอก: ฿$toCopy", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "คัดลอกผลลัพธ์",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (financeState.discountAmount > 0) {
                            SummaryRow(
                                label = "ส่วนลดที่ประหยัดได้:",
                                value = "- ฿${df.format(financeState.discountAmount)}",
                                valueColor = MaterialTheme.colorScheme.error
                            )
                        }

                        if (isExtractMode) {
                            // โหมดถอด VAT: แสดงยอดรวม -> ถอด VAT -> ได้ราคาสุทธิก่อนภาษี
                            SummaryRow(
                                label = "ยอดรวมภาษีที่จ่าย (Gross):",
                                value = "฿${df.format(financeState.finalTotal)}"
                            )

                            SummaryRow(
                                label = "ภาษีมูลค่าเพิ่มถอดออก (${financeState.vatPercent}%):",
                                value = "฿${df.format(financeState.vatAmount)}",
                                valueColor = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Column {
                                Text(
                                    text = "ยอดราคาสุทธิก่อนภาษี (Net):",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "฿${df.format(financeState.netBeforeVat)}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "สูตร: ยอดรวม × 100 ÷ (100 + ${financeState.vatPercent})",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        } else {
                            // โหมดบวก VAT: แสดงราคาก่อนภาษี -> บวก VAT -> ได้ยอดรวมทั้งสิ้น
                            SummaryRow(
                                label = "ราคาสุทธิก่อนภาษี (Net):",
                                value = "฿${df.format(financeState.netBeforeVat)}"
                            )

                            SummaryRow(
                                label = "ภาษีมูลค่าเพิ่มบวกเพิ่ม (${financeState.vatPercent}%):",
                                value = "+ ฿${df.format(financeState.vatAmount)}",
                                valueColor = MaterialTheme.colorScheme.primary
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Column {
                                Text(
                                    text = "ยอดสุทธิที่ต้องจ่ายรวมภาษี:",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "฿${df.format(financeState.finalTotal)}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- Split Bill & Service Charge ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Bill amount
                OutlinedTextField(
                    value = financeState.splitBillAmount,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() || c == '.' }
                        viewModel.updateSplitBill(filtered, financeState.splitTipPercent, financeState.splitPeopleCount)
                    },
                    label = { Text("ยอดรวมบิลค่าอาหาร/บริการ (บาท)") },
                    prefix = { Text("฿ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                // Tip / Service Charge %
                Column {
                    OutlinedTextField(
                        value = financeState.splitTipPercent,
                        onValueChange = {
                            val filtered = it.filter { c -> c.isDigit() || c == '.' }
                            viewModel.updateSplitBill(financeState.splitBillAmount, filtered, financeState.splitPeopleCount)
                        },
                        label = { Text("ทิป / เซอร์วิสชาร์จ (%)") },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0", "5", "10", "15", "17").forEach { preset ->
                            val label = if (preset == "10") "10% (SVC)" else if (preset == "17") "17% (+VAT)" else "$preset%"
                            SuggestionChip(
                                onClick = {
                                    viewModel.updateSplitBill(financeState.splitBillAmount, preset, financeState.splitPeopleCount)
                                },
                                label = { Text(label) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (financeState.splitTipPercent == preset)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }

                // Number of people
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = "จำนวนคนหาร",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${financeState.splitPeopleCount} คน",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = {
                                    if (financeState.splitPeopleCount > 1) {
                                        viewModel.updateSplitBill(
                                            financeState.splitBillAmount,
                                            financeState.splitTipPercent,
                                            financeState.splitPeopleCount - 1
                                        )
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                enabled = financeState.splitPeopleCount > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "ลดคน")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledIconButton(
                                onClick = {
                                    viewModel.updateSplitBill(
                                        financeState.splitBillAmount,
                                        financeState.splitTipPercent,
                                        financeState.splitPeopleCount + 1
                                    )
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "เพิ่มคน")
                            }
                        }
                    }
                }

                // Split Bill Summary Card
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
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ยอดจ่ายต่อคน",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "฿${df.format(financeState.splitPerPerson)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        SummaryRow(
                            label = "ยอดรวมบิลทั้งหมด (รวมทิป/SVC):",
                            value = "฿${df.format(financeState.splitTotalWithTip)}"
                        )

                        SummaryRow(
                            label = "หารเฉลี่ยจำนวน:",
                            value = "${financeState.splitPeopleCount} คน"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
