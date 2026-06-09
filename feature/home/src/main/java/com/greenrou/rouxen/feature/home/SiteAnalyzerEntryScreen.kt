package com.greenrou.rouxen.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.koin.androidx.compose.koinViewModel

@Composable
fun SiteAnalyzerEntryScreen(
    onAnalyze: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val input by viewModel.input.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background)
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Site Analyzer",
            style = RouxenTypography.titleMedium,
            color = RouxenColors.Accent,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "DNS · SSL · Headers · Ping · Whois · Traceroute",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextSecondary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        RouxenCard {
            RouxenTextField(
                value = input,
                onValueChange = viewModel::onInputChange,
                placeholder = "https://example.com",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(
                    onGo = { viewModel.buildAnalyzeUrl()?.let(onAnalyze) },
                ),
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBadge(label = error!!, status = BadgeStatus.Error)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.buildAnalyzeUrl()?.let(onAnalyze) },
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
