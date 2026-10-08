package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.CalculatorViewModel
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: CalculatorViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    // Handle back button on sub-screens to return to Calculator
    BackHandler(enabled = currentTab != AppTab.CALCULATOR) {
        viewModel.setTab(AppTab.CALCULATOR)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.CALCULATOR,
                    onClick = { viewModel.setTab(AppTab.CALCULATOR) },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "เครื่องคิดเลข") },
                    label = { Text("คิดเลข", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_calculator"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { viewModel.setTab(AppTab.HISTORY) },
                    icon = { Icon(Icons.Default.History, contentDescription = "ประวัติ") },
                    label = { Text("ประวัติ", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_history"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.FINANCE,
                    onClick = { viewModel.setTab(AppTab.FINANCE) },
                    icon = { Icon(Icons.Default.LocalOffer, contentDescription = "ภาษี/ส่วนลด") },
                    label = { Text("ภาษี/ลด", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_finance"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CONVERTER,
                    onClick = { viewModel.setTab(AppTab.CONVERTER) },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "แปลงหน่วย") },
                    label = { Text("แปลงหน่วย", fontSize = 12.sp) },
                    modifier = Modifier.testTag("nav_converter"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (currentTab) {
            AppTab.CALCULATOR -> CalculatorScreen(viewModel = viewModel, modifier = screenModifier)
            AppTab.HISTORY -> HistoryScreen(viewModel = viewModel, modifier = screenModifier)
            AppTab.FINANCE -> FinanceScreen(viewModel = viewModel, modifier = screenModifier)
            AppTab.CONVERTER -> ConverterScreen(viewModel = viewModel, modifier = screenModifier)
        }
    }
}
