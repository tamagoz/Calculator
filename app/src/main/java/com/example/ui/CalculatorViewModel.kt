package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationEntity
import com.example.data.local.CalculationRepository
import com.example.engine.CalculatorEngine
import com.example.engine.EvalResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

enum class AppTab(val titleThai: String, val titleEng: String) {
    CALCULATOR("เครื่องคิดเลข", "Calculator"),
    HISTORY("ประวัติ", "History"),
    FINANCE("ส่วนลด/ภาษี", "Tax & Split"),
    CONVERTER("แปลงหน่วย", "Converter")
}

data class CalculatorUiState(
    val expression: String = "",
    val previewResult: String = "",
    val isScientificOpen: Boolean = false,
    val isDegreeMode: Boolean = true,
    val isLastEvaluated: Boolean = false,
    val errorMessage: String? = null
)

enum class VatCalculationMode(val title: String, val shortDesc: String) {
    EXTRACT_VAT("ถอด VAT (ใน)", "ยอดรวมภาษี → ราคาก่อนภาษี"),
    ADD_VAT("บวก VAT (นอก)", "ราคาก่อนภาษี → รวมภาษี")
}

data class FinanceUiState(
    val vatMode: VatCalculationMode = VatCalculationMode.EXTRACT_VAT,
    val originalPrice: String = "1070",
    val discountPercent: String = "0",
    val vatPercent: String = "7", // Thailand VAT standard is 7%
    val discountAmount: Double = 0.0,
    val netBeforeVat: Double = 1000.0,
    val vatAmount: Double = 70.0,
    val finalTotal: Double = 1070.0,

    // Split Bill
    val splitBillAmount: String = "1200",
    val splitTipPercent: String = "10",
    val splitPeopleCount: Int = 4,
    val splitTotalWithTip: Double = 1320.0,
    val splitPerPerson: Double = 330.0
)

enum class UnitCategory(val title: String) {
    PRESSURE("แรงดัน (Pressure)"),
    LENGTH("ความยาว (Length)"),
    WEIGHT("น้ำหนัก (Weight)"),
    AREA("พื้นที่ (Area / ไทย)"),
    TEMPERATURE("อุณหภูมิ (Temp)"),
    DATA("ข้อมูลดิจิทัล (Data)"),
    VOLTAGE("แรงดันไฟฟ้า (Voltage)")
}

data class UnitItem(val id: String, val name: String, val toBaseFactor: Double)

data class ConverterUiState(
    val category: UnitCategory = UnitCategory.LENGTH,
    val inputValue: String = "1",
    val fromUnitIndex: Int = 0,
    val toUnitIndex: Int = 1,
    val resultValue: String = "100"
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalculationRepository
    private val engine = CalculatorEngine(isDegreeMode = true)

    private val _currentTab = MutableStateFlow(AppTab.CALCULATOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    private val _financeState = MutableStateFlow(FinanceUiState())
    val financeState: StateFlow<FinanceUiState> = _financeState.asStateFlow()

    private val _converterState = MutableStateFlow(ConverterUiState())
    val converterState: StateFlow<ConverterUiState> = _converterState.asStateFlow()

    val historyList: StateFlow<List<CalculationEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CalculationRepository(database.calculationDao())
        historyList = repository.history.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        recalculateFinance()
        recalculateConverter()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Calculator Engine Actions ---

    fun onDigitClick(digit: String) {
        val current = _calcState.value
        val newExpr = if (current.isLastEvaluated) {
            digit
        } else {
            current.expression + digit
        }
        updateExpression(newExpr, isLastEvaluated = false)
    }

    fun onOperatorClick(op: String) {
        val current = _calcState.value
        val expr = current.expression

        if (expr.isEmpty()) {
            if (op == "−") {
                updateExpression("−", isLastEvaluated = false)
            }
            return
        }

        val lastChar = expr.last().toString()
        val operators = listOf("+", "−", "×", "÷")

        val newExpr = if (operators.contains(lastChar)) {
            // Replace trailing operator
            expr.dropLast(1) + op
        } else {
            expr + op
        }
        updateExpression(newExpr, isLastEvaluated = false)
    }

    fun onFunctionClick(func: String) {
        val current = _calcState.value
        val expr = if (current.isLastEvaluated) "" else current.expression
        val newExpr = when (func) {
            "π" -> expr + "π"
            "e" -> expr + "e"
            "x²" -> expr + "^2"
            "x^y" -> expr + "^"
            "√" -> expr + "√("
            "!" -> expr + "!"
            "sin" -> expr + "sin("
            "cos" -> expr + "cos("
            "tan" -> expr + "tan("
            "ln" -> expr + "ln("
            "log" -> expr + "log("
            else -> expr + func
        }
        updateExpression(newExpr, isLastEvaluated = false)
    }

    fun onParenthesisClick() {
        val current = _calcState.value
        val expr = if (current.isLastEvaluated) "" else current.expression
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }

        val newExpr = if (openCount > closeCount && expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e')) {
            expr + ")"
        } else {
            if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == ')' || expr.last() == 'π' || expr.last() == 'e')) {
                expr + "×("
            } else {
                expr + "("
            }
        }
        updateExpression(newExpr, isLastEvaluated = false)
    }

    fun onDecimalClick() {
        val current = _calcState.value
        val expr = if (current.isLastEvaluated) "" else current.expression

        // Get current number token
        val lastNumber = expr.takeLastWhile { it.isDigit() || it == '.' }
        if (!lastNumber.contains('.')) {
            val newExpr = if (lastNumber.isEmpty() || (!expr.last().isDigit())) {
                expr + "0."
            } else {
                expr + "."
            }
            updateExpression(newExpr, isLastEvaluated = false)
        }
    }

    fun onToggleSignClick() {
        val current = _calcState.value
        val expr = current.expression
        if (expr.isEmpty()) return

        // Toggle sign on leading expression or wrap in negate
        if (expr.startsWith("−")) {
            updateExpression(expr.substring(1), isLastEvaluated = current.isLastEvaluated)
        } else {
            updateExpression("−$expr", isLastEvaluated = current.isLastEvaluated)
        }
    }

    fun onPercentageClick() {
        val current = _calcState.value
        val expr = current.expression
        if (expr.isEmpty()) return
        val last = expr.last()
        if (last.isDigit() || last == ')') {
            updateExpression(expr + "%", isLastEvaluated = false)
        }
    }

    fun onClearClick() {
        _calcState.value = CalculatorUiState(
            isScientificOpen = _calcState.value.isScientificOpen,
            isDegreeMode = _calcState.value.isDegreeMode
        )
    }

    fun onBackspaceClick() {
        val current = _calcState.value
        if (current.isLastEvaluated) {
            onClearClick()
            return
        }
        if (current.expression.isNotEmpty()) {
            val expr = current.expression
            val newExpr = when {
                expr.endsWith("sin(") || expr.endsWith("cos(") || expr.endsWith("tan(") || expr.endsWith("log(") -> expr.drop(4)
                expr.endsWith("ln(") || expr.endsWith("√(") -> expr.drop(3)
                else -> expr.dropLast(1)
            }
            updateExpression(newExpr, isLastEvaluated = false)
        }
    }

    fun onEqualsClick() {
        val current = _calcState.value
        if (current.expression.isBlank()) return

        when (val result = engine.evaluate(current.expression)) {
            is EvalResult.Success -> {
                val formatted = result.formatted
                viewModelScope.launch {
                    repository.saveCalculation(current.expression, formatted)
                }
                _calcState.value = current.copy(
                    expression = formatted,
                    previewResult = "",
                    isLastEvaluated = true,
                    errorMessage = null
                )
            }
            is EvalResult.Error -> {
                _calcState.value = current.copy(
                    errorMessage = result.message
                )
            }
        }
    }

    fun onToggleScientific() {
        _calcState.value = _calcState.value.copy(
            isScientificOpen = !_calcState.value.isScientificOpen
        )
    }

    fun onToggleDegreeRad() {
        val newDeg = !_calcState.value.isDegreeMode
        engine.isDegreeMode = newDeg
        _calcState.value = _calcState.value.copy(isDegreeMode = newDeg)
        updatePreview(_calcState.value.expression)
    }

    fun loadFromHistory(item: CalculationEntity) {
        _calcState.value = _calcState.value.copy(
            expression = item.expression,
            previewResult = "= ${item.result}",
            isLastEvaluated = false,
            errorMessage = null
        )
        _currentTab.value = AppTab.CALCULATOR
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private fun updateExpression(newExpr: String, isLastEvaluated: Boolean) {
        _calcState.value = _calcState.value.copy(
            expression = newExpr,
            isLastEvaluated = isLastEvaluated,
            errorMessage = null
        )
        updatePreview(newExpr)
    }

    private fun updatePreview(expr: String) {
        if (expr.isBlank()) {
            _calcState.value = _calcState.value.copy(previewResult = "")
            return
        }
        // If expr ends with operator, don't show error preview
        val last = expr.lastOrNull()
        if (last == '+' || last == '−' || last == '×' || last == '÷' || last == '(' || last == '^') {
            _calcState.value = _calcState.value.copy(previewResult = "")
            return
        }

        when (val res = engine.evaluate(expr)) {
            is EvalResult.Success -> {
                _calcState.value = _calcState.value.copy(previewResult = "= ${res.formatted}")
            }
            is EvalResult.Error -> {
                _calcState.value = _calcState.value.copy(previewResult = "")
            }
        }
    }

    // --- Finance / Discount & 7% VAT & Split Bill ---

    fun updateOriginalPrice(price: String) {
        _financeState.value = _financeState.value.copy(originalPrice = price)
        recalculateFinance()
    }

    fun updateDiscountPercent(percent: String) {
        _financeState.value = _financeState.value.copy(discountPercent = percent)
        recalculateFinance()
    }

    fun updateVatPercent(vat: String) {
        _financeState.value = _financeState.value.copy(vatPercent = vat)
        recalculateFinance()
    }

    fun setVatMode(mode: VatCalculationMode) {
        val currentPrice = _financeState.value.originalPrice
        val updatedPrice = if (currentPrice == "1000" && mode == VatCalculationMode.EXTRACT_VAT) {
            "1070"
        } else if (currentPrice == "1070" && mode == VatCalculationMode.ADD_VAT) {
            "1000"
        } else {
            currentPrice
        }
        _financeState.value = _financeState.value.copy(
            vatMode = mode,
            originalPrice = updatedPrice
        )
        recalculateFinance()
    }

    private fun recalculateFinance() {
        val state = _financeState.value
        val inputPrice = state.originalPrice.toDoubleOrNull() ?: 0.0
        val discountPct = state.discountPercent.toDoubleOrNull() ?: 0.0
        val vatPct = state.vatPercent.toDoubleOrNull() ?: 0.0

        if (state.vatMode == VatCalculationMode.EXTRACT_VAT) {
            // ถอด VAT: inputPrice คือ ยอดรวมภาษีที่ต้องจ่าย (Gross price incl. VAT)
            val discountAmt = inputPrice * (discountPct / 100.0)
            val grossAfterDiscount = (inputPrice - discountAmt).coerceAtLeast(0.0)
            val netBeforeVat = if (vatPct > 0) {
                grossAfterDiscount * 100.0 / (100.0 + vatPct)
            } else {
                grossAfterDiscount
            }
            val vatAmt = grossAfterDiscount - netBeforeVat

            _financeState.value = state.copy(
                discountAmount = discountAmt,
                netBeforeVat = netBeforeVat,
                vatAmount = vatAmt,
                finalTotal = grossAfterDiscount
            )
        } else {
            // บวก VAT: inputPrice คือ ราคาสุทธิก่อนภาษี (Net price excl. VAT)
            val discountAmt = inputPrice * (discountPct / 100.0)
            val netBeforeVat = (inputPrice - discountAmt).coerceAtLeast(0.0)
            val vatAmt = netBeforeVat * (vatPct / 100.0)
            val total = netBeforeVat + vatAmt

            _financeState.value = state.copy(
                discountAmount = discountAmt,
                netBeforeVat = netBeforeVat,
                vatAmount = vatAmt,
                finalTotal = total
            )
        }
    }

    fun updateSplitBill(amount: String, tip: String, people: Int) {
        val totalAmt = amount.toDoubleOrNull() ?: 0.0
        val tipPct = tip.toDoubleOrNull() ?: 0.0
        val p = people.coerceAtLeast(1)

        val tipAmt = totalAmt * (tipPct / 100.0)
        val totalWithTip = totalAmt + tipAmt
        val perPerson = totalWithTip / p

        _financeState.value = _financeState.value.copy(
            splitBillAmount = amount,
            splitTipPercent = tip,
            splitPeopleCount = p,
            splitTotalWithTip = totalWithTip,
            splitPerPerson = perPerson
        )
    }

    // --- Unit Converter ---

    fun setUnitCategory(category: UnitCategory) {
        _converterState.value = _converterState.value.copy(
            category = category,
            fromUnitIndex = 0,
            toUnitIndex = 1
        )
        recalculateConverter()
    }

    fun setConverterInput(value: String) {
        _converterState.value = _converterState.value.copy(inputValue = value)
        recalculateConverter()
    }

    fun setFromUnitIndex(index: Int) {
        _converterState.value = _converterState.value.copy(fromUnitIndex = index)
        recalculateConverter()
    }

    fun setToUnitIndex(index: Int) {
        _converterState.value = _converterState.value.copy(toUnitIndex = index)
        recalculateConverter()
    }

    fun swapUnits() {
        val s = _converterState.value
        _converterState.value = s.copy(
            fromUnitIndex = s.toUnitIndex,
            toUnitIndex = s.fromUnitIndex
        )
        recalculateConverter()
    }

    private fun recalculateConverter() {
        val s = _converterState.value
        val input = s.inputValue.toDoubleOrNull() ?: 0.0
        val units = getUnitsForCategory(s.category)

        if (units.isEmpty() || s.fromUnitIndex !in units.indices || s.toUnitIndex !in units.indices) {
            _converterState.value = s.copy(resultValue = "0")
            return
        }

        val fromUnit = units[s.fromUnitIndex]
        val toUnit = units[s.toUnitIndex]

        val result = if (s.category == UnitCategory.TEMPERATURE) {
            convertTemperature(input, fromUnit.id, toUnit.id)
        } else {
            // Factor based conversion
            val baseValue = input * fromUnit.toBaseFactor
            baseValue / toUnit.toBaseFactor
        }

        val formatted = try {
            val df = DecimalFormat("#,##0.######")
            df.format(BigDecimal.valueOf(result).setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toDouble())
        } catch (e: Exception) {
            result.toString()
        }

        _converterState.value = s.copy(resultValue = formatted)
    }

    fun getUnitsForCategory(category: UnitCategory): List<UnitItem> {
        return when (category) {
            UnitCategory.PRESSURE -> listOf(
                UnitItem("bar", "บาร์ (bar)", 100000.0),
                UnitItem("psi", "ปอนด์/ตร.นิ้ว (psi)", 6894.75729),
                UnitItem("kpa", "กิโลพาสคัล (kPa)", 1000.0),
                UnitItem("mpa", "เมกะพาสคัล (MPa)", 1000000.0),
                UnitItem("pa", "พาสคัล (Pa)", 1.0),
                UnitItem("atm", "บรรยากาศ (atm)", 101325.0),
                UnitItem("kgcm2", "กก./ตร.ซม. (kg/cm²)", 98066.5),
                UnitItem("mmhg", "มม.ปรอท (mmHg / Torr)", 133.322368),
                UnitItem("mbar", "มิลลิบาร์ (mbar)", 100.0)
            )
            UnitCategory.VOLTAGE -> listOf(
                UnitItem("v", "โวลต์ (V)", 1.0),
                UnitItem("mv", "มิลลิโวลต์ (mV)", 0.001),
                UnitItem("kv", "กิโลโวลต์ (kV)", 1000.0),
                UnitItem("uv", "ไมโครโวลต์ (µV)", 0.000001),
                UnitItem("mv_mega", "เมกะโวลต์ (MV)", 1000000.0)
            )
            UnitCategory.LENGTH -> listOf(
                UnitItem("m", "เมตร (m)", 1.0),
                UnitItem("cm", "เซนติเมตร (cm)", 0.01),
                UnitItem("km", "กิโลเมตร (km)", 1000.0),
                UnitItem("mm", "มิลลิเมตร (mm)", 0.001),
                UnitItem("in", "นิ้ว (in)", 0.0254),
                UnitItem("ft", "ฟุต (ft)", 0.3048),
                UnitItem("yd", "หลา (yd)", 0.9144),
                UnitItem("mi", "ไมล์ (mi)", 1609.344),
                UnitItem("wa", "วา (ไทย)", 2.0)
            )
            UnitCategory.WEIGHT -> listOf(
                UnitItem("kg", "กิโลกรัม (kg)", 1.0),
                UnitItem("g", "กรัม (g)", 0.001),
                UnitItem("mg", "มิลลิกรัม (mg)", 0.000001),
                UnitItem("lb", "ปอนด์ (lb)", 0.45359237),
                UnitItem("oz", "ออนซ์ (oz)", 0.0283495),
                UnitItem("ton", "ตัน (t)", 1000.0),
                UnitItem("khit", "ขีด (ไทย - 100g)", 0.1),
                UnitItem("tamlueng", "ตำลึง (ไทย - 60g)", 0.06),
                UnitItem("baht", "บาท (ทอง - 15.2g)", 0.015244)
            )
            UnitCategory.AREA -> listOf(
                UnitItem("sqm", "ตารางเมตร (m²)", 1.0),
                UnitItem("rai", "ไร่ (ไทย - 1,600 m²)", 1600.0),
                UnitItem("ngan", "งาน (ไทย - 400 m²)", 400.0),
                UnitItem("sqwa", "ตารางวา (ไทย - 4 m²)", 4.0),
                UnitItem("sqkm", "ตารางกิโลเมตร (km²)", 1000000.0),
                UnitItem("sqft", "ตารางฟุต (ft²)", 0.092903),
                UnitItem("acre", "เอเคอร์ (acre)", 4046.86)
            )
            UnitCategory.TEMPERATURE -> listOf(
                UnitItem("C", "เซลเซียส (°C)", 1.0),
                UnitItem("F", "ฟาเรนไฮต์ (°F)", 1.0),
                UnitItem("K", "เคลวิน (K)", 1.0)
            )
            UnitCategory.DATA -> listOf(
                UnitItem("B", "ไบต์ (Bytes)", 1.0),
                UnitItem("KB", "กิโลไบต์ (KB)", 1024.0),
                UnitItem("MB", "เมกะไบต์ (MB)", 1048576.0),
                UnitItem("GB", "กิกะไบต์ (GB)", 1073741824.0),
                UnitItem("TB", "เทระไบต์ (TB)", 1099511627776.0)
            )
        }
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        if (from == to) return value
        // Convert to Celsius first
        val c = when (from) {
            "C" -> value
            "F" -> (value - 32.0) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }
        // Convert Celsius to target
        return when (to) {
            "C" -> c
            "F" -> (c * 9.0 / 5.0) + 32.0
            "K" -> c + 273.15
            else -> c
        }
    }
}
