package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.calcState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Upper section: Display
        CalculatorDisplay(
            state = state,
            onToggleDegreeRad = { viewModel.onToggleDegreeRad() },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Lower section: Keypad
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            CalculatorKeypad(
                isScientificOpen = state.isScientificOpen,
                onDigitClick = { viewModel.onDigitClick(it) },
                onOperatorClick = { viewModel.onOperatorClick(it) },
                onFunctionClick = { viewModel.onFunctionClick(it) },
                onParenthesisClick = { viewModel.onParenthesisClick() },
                onPercentageClick = { viewModel.onPercentageClick() },
                onToggleSignClick = { viewModel.onToggleSignClick() },
                onDecimalClick = { viewModel.onDecimalClick() },
                onClearClick = { viewModel.onClearClick() },
                onBackspaceClick = { viewModel.onBackspaceClick() },
                onEqualsClick = { viewModel.onEqualsClick() },
                onToggleScientific = { viewModel.onToggleScientific() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
