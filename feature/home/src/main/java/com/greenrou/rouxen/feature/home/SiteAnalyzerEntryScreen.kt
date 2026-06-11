package com.greenrou.rouxen.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.RouxenTextField
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.history.HistoryScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun SiteAnalyzerEntryScreen(
    onBack: () -> Unit,
    onAnalyze: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val input by viewModel.input.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        SiteAnalyzerHeader(onBack = onBack)

        var selectedTab by remember { mutableStateOf(0) }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = RouxenColors.Surface,
            contentColor = RouxenColors.Accent,
            indicator = {},
            divider = {},
        ) {
            listOf("Analyze", "History").forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = label,
                            style = RouxenTypography.labelMedium,
                            color = if (selectedTab == index) RouxenColors.Accent else RouxenColors.TextSecondary,
                        )
                    },
                )
            }
        }

        if (selectedTab == 0) {
            AnalyzeTab(
                input = input,
                error = error,
                onInputChange = viewModel::onInputChange,
                onAnalyzeClick = { viewModel.buildAnalyzeUrl()?.let(onAnalyze) },
            )
        } else {
            HistoryScreen(onReanalyze = onAnalyze)
        }
    }
}

@Composable
private fun AnalyzeTab(
    input: String,
    error: String?,
    onInputChange: (String) -> Unit,
    onAnalyzeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DNS · SSL · Headers · Ping · Whois · Traceroute",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextSecondary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        RouxenCard {
            RouxenTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = "https://example.com",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(
                    onGo = { onAnalyzeClick() },
                ),
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBadge(label = error, status = BadgeStatus.Error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAnalyzeClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouxenColors.Accent,
                    contentColor = RouxenColors.Background,
                ),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(text = "Analyze", style = RouxenTypography.labelMedium)
            }
        }
    }
}

@Composable
private fun SiteAnalyzerHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text("← Home", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
        }
        Text(
            text = "Site Analyzer",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
