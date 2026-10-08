package com.example

import com.example.engine.CalculatorEngine
import com.example.engine.EvalResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val engine = CalculatorEngine(isDegreeMode = true)

    @Test
    fun testBasicArithmetic() {
        val res1 = engine.evaluate("15 + 25")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("40", (res1 as EvalResult.Success).formatted)

        val res2 = engine.evaluate("100 − 35")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("65", (res2 as EvalResult.Success).formatted)

        val res3 = engine.evaluate("12 × 5")
        assertTrue(res3 is EvalResult.Success)
        assertEquals("60", (res3 as EvalResult.Success).formatted)

        val res4 = engine.evaluate("100 ÷ 4")
        assertTrue(res4 is EvalResult.Success)
        assertEquals("25", (res4 as EvalResult.Success).formatted)
    }

    @Test
    fun testPrecedenceAndParentheses() {
        val res1 = engine.evaluate("2 + 3 × 4")
        assertTrue(res1 is EvalResult.Success)
        assertEquals("14", (res1 as EvalResult.Success).formatted)

        val res2 = engine.evaluate("(2 + 3) × 4")
        assertTrue(res2 is EvalResult.Success)
        assertEquals("20", (res2 as EvalResult.Success).formatted)
    }

    @Test
    fun testScientificFunctions() {
        val resSin = engine.evaluate("sin(30)")
        assertTrue(resSin is EvalResult.Success)
        assertEquals(0.5, (resSin as EvalResult.Success).value, 0.0001)

        val resSqrt = engine.evaluate("√(16)")
        assertTrue(resSqrt is EvalResult.Success)
        assertEquals("4", (resSqrt as EvalResult.Success).formatted)

        val resPower = engine.evaluate("2^8")
        assertTrue(resPower is EvalResult.Success)
        assertEquals("256", (resPower as EvalResult.Success).formatted)

        val resFactorial = engine.evaluate("5!")
        assertTrue(resFactorial is EvalResult.Success)
        assertEquals("120", (resFactorial as EvalResult.Success).formatted)
    }

    @Test
    fun testDivideByZero() {
        val res = engine.evaluate("10 ÷ 0")
        assertTrue(res is EvalResult.Error)
    }

    @Test
    fun testVatExtraction() {
        // ถอด VAT: ยอดรวม 1,070 บาท VAT 7% -> ราคาก่อนภาษี 1,000 บาท, VAT 70 บาท
        val gross = 1070.0
        val vatPct = 7.0
        val netBeforeVat = gross * 100.0 / (100.0 + vatPct)
        val vatAmt = gross - netBeforeVat

        assertEquals(1000.0, netBeforeVat, 0.001)
        assertEquals(70.0, vatAmt, 0.001)

        // บวก VAT: ราคาก่อนภาษี 1,000 บาท VAT 7% -> ยอดรวม 1,070 บาท
        val net = 1000.0
        val vatAdded = net * (vatPct / 100.0)
        val grossTotal = net + vatAdded

        assertEquals(70.0, vatAdded, 0.001)
        assertEquals(1070.0, grossTotal, 0.001)
    }

    @Test
    fun testPressureConversion() {
        val barFactor = 100000.0 // 1 bar = 100,000 Pa
        val psiFactor = 6894.75729 // 1 psi = 6,894.757 Pa
        val kpaFactor = 1000.0 // 1 kPa = 1,000 Pa

        // Convert 2 bar to psi: 2 * 100000 / 6894.75729 ≈ 29.0075 psi
        val twoBarInPsi = (2.0 * barFactor) / psiFactor
        assertEquals(29.0075, twoBarInPsi, 0.01)

        // Convert 1 bar to kPa: 100 kPa
        val oneBarInKpa = (1.0 * barFactor) / kpaFactor
        assertEquals(100.0, oneBarInKpa, 0.001)
    }
}
