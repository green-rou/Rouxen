package com.greenrou.rouxen.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTheme

private val TextFieldShape = RoundedCornerShape(4.dp)

@Composable
fun RouxenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.border(
            width = 1.dp,
            color = RouxenColors.Border,
            shape = TextFieldShape,
        ),
        placeholder = {
            Text(
                text = placeholder,
                color = RouxenColors.TextSecondary,
            )
        },
        leadingIcon = leadingIcon,
        singleLine = singleLine,
        shape = TextFieldShape,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = TextFieldDefaults.colors(
            focusedTextColor = RouxenColors.TextPrimary,
            unfocusedTextColor = RouxenColors.TextPrimary,
            focusedContainerColor = RouxenColors.Surface,
            unfocusedContainerColor = RouxenColors.Surface,
            cursorColor = RouxenColors.Accent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun RouxenTextFieldPreview() {
    RouxenTheme {
        RouxenTextField(
            value = "",
            onValueChange = {},
            placeholder = "https://example.com",
        )
    }
}
