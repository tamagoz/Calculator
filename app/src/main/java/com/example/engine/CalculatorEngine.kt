package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

class CalculatorEngine(var isDegreeMode: Boolean = true) {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    private val decimalFormat = DecimalFormat("#,##0.##########", symbols)
    private val scientificFormat = DecimalFormat("0.######E0", symbols)

    fun evaluate(expression: String): EvalResult {
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) {
            return EvalResult.Success(0.0, "0")
        }

        return try {
            val parser = ExpressionParser(sanitized, isDegreeMode)
            val value = parser.parse()
            if (value.isNaN()) {
                EvalResult.Error("ข้อผิดพลาดทางคณิตศาสตร์")
            } else if (value.isInfinite()) {
                EvalResult.Error("ไม่สามารถหารด้วยศูนย์ได้")
            } else {
                EvalResult.Success(value, formatNumber(value))
            }
        } catch (e: ArithmeticException) {
            EvalResult.Error(e.message ?: "ข้อผิดพลาดการคำนวณ")
        } catch (e: Exception) {
            EvalResult.Error("รูปแบบไม่ถูกต้อง")
        }
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"

        val bd = try {
            BigDecimal.valueOf(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        } catch (e: Exception) {
            null
        }

        val roundedVal = bd?.toDouble() ?: value
        val absVal = kotlin.math.abs(roundedVal)

        return if (absVal >= 1e12 || (absVal > 0 && absVal < 1e-6)) {
            scientificFormat.format(roundedVal)
        } else {
            decimalFormat.format(roundedVal)
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .trim()
    }

    private class ExpressionParser(private val input: String, private val isDegreeMode: Boolean) {
        private var pos = 0
        private var ch = if (input.isNotEmpty()) input[0] else '\u0000'

        private fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            if (input.isBlank()) return 0.0
            val x = parseExpression()
            while (ch == ' ') nextChar()
            if (ch != '\u0000') {
                throw IllegalArgumentException("Unexpected character: $ch")
            }
            return x
        }

        // Expression = Term (+/- Term)*
        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        // Term = Factor (* / % Factor)*
        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("ไม่สามารถหารด้วยศูนย์ได้")
                        x /= divisor
                    }
                    eat('%') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("ไม่สามารถหารด้วยศูนย์ได้")
                        x %= divisor
                    }
                    else -> return x
                }
            }
        }

        // Factor = (+/- Factor) or (Base ^ Exponent)
        private fun parseFactor(): Double {
            while (ch == ' ') nextChar()
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if (eat('π')) {
                x = Math.PI
            } else if (ch == 'e' && (pos + 1 >= input.length || !input[pos + 1].isLetter())) {
                nextChar()
                x = Math.E
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                val numStr = input.substring(startPos, pos)
                x = numStr.toDouble()
            } else if (ch in 'a'..'z' || ch in 'A'..'Z' || ch == '√') {
                if (ch == '√') {
                    nextChar()
                    val arg = if (eat('(')) {
                        val v = parseExpression()
                        eat(')')
                        v
                    } else {
                        parseFactor()
                    }
                    if (arg < 0) throw ArithmeticException("ไม่สามารถหาค่ารากที่สองของเลขติดลบได้")
                    x = sqrt(arg)
                } else {
                    while (ch in 'a'..'z' || ch in 'A'..'Z') nextChar()
                    val func = input.substring(startPos, pos).lowercase()
                    val arg = if (eat('(')) {
                        val v = parseExpression()
                        eat(')')
                        v
                    } else {
                        parseFactor()
                    }

                    x = when (func) {
                        "sin" -> {
                            val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                            sin(rad)
                        }
                        "cos" -> {
                            val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                            cos(rad)
                        }
                        "tan" -> {
                            val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                            tan(rad)
                        }
                        "asin" -> {
                            val res = asin(arg)
                            if (isDegreeMode) Math.toDegrees(res) else res
                        }
                        "acos" -> {
                            val res = acos(arg)
                            if (isDegreeMode) Math.toDegrees(res) else res
                        }
                        "atan" -> {
                            val res = atan(arg)
                            if (isDegreeMode) Math.toDegrees(res) else res
                        }
                        "sqrt" -> {
                            if (arg < 0) throw ArithmeticException("รากที่สองของเลขติดลบ")
                            sqrt(arg)
                        }
                        "log" -> {
                            if (arg <= 0) throw ArithmeticException("log ของเลขน้อยกว่าหรือเท่ากับ 0")
                            log10(arg)
                        }
                        "ln" -> {
                            if (arg <= 0) throw ArithmeticException("ln ของเลขน้อยกว่าหรือเท่ากับ 0")
                            ln(arg)
                        }
                        "abs" -> kotlin.math.abs(arg)
                        "exp" -> exp(arg)
                        else -> throw IllegalArgumentException("Unknown function: $func")
                    }
                }
            } else {
                return 0.0
            }

            // Check for postfix factorial '!' or percentage or power '^'
            while (true) {
                when {
                    eat('!') -> {
                        val n = x.toLong()
                        if (n < 0 || n > 20 || x != n.toDouble()) {
                            throw ArithmeticException("แฟกทอเรียลรองรับเฉพาะจำนวนเต็มบวก 0-20")
                        }
                        var fact = 1L
                        for (i in 2..n) fact *= i
                        x = fact.toDouble()
                    }
                    eat('^') -> {
                        val exp = parseFactor()
                        x = x.pow(exp)
                    }
                    else -> break
                }
            }

            return x
        }
    }
}
