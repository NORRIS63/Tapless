package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RenameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        modifier = Modifier.border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp)),
        title = {
            Text(
                text = stringResource(R.string.rename_title),
                color = TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                singleLine = true,
                placeholder = { Text(stringResource(R.string.rename_hint), color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealAccent,
                    unfocusedBorderColor = DarkDivider,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = Color(0xFF13151A),
                    unfocusedContainerColor = Color(0xFF13151A)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_text_field")
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = currentName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealAccent,
                    contentColor = Color(0xFF101114)
                ),
                modifier = Modifier.testTag("confirm_rename_button")
            ) {
                Text(stringResource(R.string.save), color = Color(0xFF101114))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_rename_button")
            ) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}
