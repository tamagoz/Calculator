package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val history: Flow<List<CalculationEntity>> = dao.getAllHistory()

    suspend fun saveCalculation(expression: String, result: String) {
        dao.insert(
            CalculationEntity(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
