package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PermissionRationaleDialog(
    onGrantClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceVariant,
        title = {
            Text(
                text = stringResource(R.string.permission_required_title),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.permission_required_desc),
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = RecordingRed),
                modifier = Modifier.testTag("grant_permission_button")
            ) {
                Text(stringResource(R.string.grant_permission), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_permission_button")
            ) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}
