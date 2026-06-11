package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.database.AppDatabase
import com.example.data.repository.ToolRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ToolViewModel
import com.example.ui.viewmodel.ToolViewModelFactory

class MainActivity : ComponentActivity() {

    private val db by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { ToolRepository(db.toolDao()) }
    private val viewModel: ToolViewModel by viewModels {
        ToolViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainLayoutContainer(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainLayoutContainer(
    viewModel: ToolViewModel,
    modifier: Modifier = Modifier
) {
    val activeToolId by viewModel.activeToolId.collectAsState()

    Scaffold(modifier = modifier.fillMaxSize()) { paddingValues ->
        val innerModifier = Modifier.padding(paddingValues)
        when (activeToolId) {
            "json_formatter" -> JsonFormatterScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "html_beautifier" -> HtmlBeautifierScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "base64" -> Base64CodecScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "url_codec" -> UrlCodecScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "html_entities" -> HtmlEntitiesScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "uuid_gen" -> UuidGeneratorScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "password_gen" -> PasswordGeneratorScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "hash_gen" -> HashGeneratorScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "case_converter" -> CaseConverterScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "epoch_converter" -> EpochConverterScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "base_converter" -> BaseConverterScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            "billing" -> BillingScreen(
                viewModel = viewModel,
                onBack = { viewModel.setActiveTool(null) }
            )
            else -> DashboardScreen(
                viewModel = viewModel,
                onToolClick = { tool -> viewModel.setActiveTool(tool.id) }
            )
        }
    }
}
