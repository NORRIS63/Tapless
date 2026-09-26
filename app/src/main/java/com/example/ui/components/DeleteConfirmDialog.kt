package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AudioRecording
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DeleteConfirmDialog(
    recording: AudioRecording,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        modifier = Modifier.border(1.dp, DarkSurfaceBorder, RoundedCornerShape(20.dp)),
        title = {
            Text(
                text = stringResource(R.string.delete_confirm_title),
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "Delete \"${recording.title}\"? This audio file will be permanently removed.",
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RecordingRed),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(stringResource(R.string.delete), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}
