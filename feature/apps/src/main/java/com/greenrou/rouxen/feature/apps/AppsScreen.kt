package com.greenrou.rouxen.feature.apps

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.RouxenTextField
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.apps.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppsScreen(viewModel: AppsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val query by viewModel.query.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) searchFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background)
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSearchActive) {
                RouxenTextField(
                    value = query,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocusRequester),
                    placeholder = "Search apps...",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = RouxenColors.TextSecondary,
                        )
                    },
                )
                IconButton(
                    onClick = {
                        viewModel.onSearchQueryChange("")
                        isSearchActive = false
                    },
                    modifier = Modifier.size(40.dp),
                ) {
                    Text(text = "×", style = RouxenTypography.titleMedium, color = RouxenColors.TextSecondary)
                }
            } else {
                Text(
                    text = "Apps",
                    style = RouxenTypography.titleMedium,
                    color = RouxenColors.Accent,
                    modifier = Modifier.weight(1f),
                )
                if (state !is AppsState.Empty) {
                    IconButton(
                        onClick = { isSearchActive = true },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = RouxenColors.TextSecondary,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val s = state) {
            is AppsState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = RouxenColors.Accent,
                    strokeWidth = 2.dp,
                )
            }

            is AppsState.Empty -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No apps found", style = RouxenTypography.bodySmall, color = RouxenColors.TextSecondary)
            }

            is AppsState.Data -> if (s.apps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No apps match \"$query\"",
                        style = RouxenTypography.bodySmall,
                        color = RouxenColors.TextSecondary,
                    )
                }
            } else LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(s.apps, key = { it.packageName }) { app ->
                    RouxenCard(modifier = Modifier.clickable { viewModel.launchApp(app.packageName) }) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.label,
                                    style = RouxenTypography.bodyMedium,
                                    color = RouxenColors.TextPrimary,
                                    maxLines = 1,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = app.packageName,
                                    style = RouxenTypography.labelSmall,
                                    color = RouxenColors.TextSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
