package com.fireflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.fireflow.app.navigation.FireFlowNavHost
import com.fireflow.common.R
import com.fireflow.common.theme.FireFlowTheme
import com.fireflow.security.RootDetector
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rootDetector: RootDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FireFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FireFlowNavHost()
                    RootWarningHost(rootDetector = rootDetector)
                }
            }
        }
    }
}

/** Non-blocking advisory dialog shown once per session on rooted devices. */
@Composable
private fun RootWarningHost(rootDetector: RootDetector) {
    var showWarning by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showWarning = rootDetector.detect().isRooted
    }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text(stringResource(R.string.root_warning_title)) },
            text = { Text(stringResource(R.string.root_warning_message)) },
            confirmButton = {
                TextButton(onClick = { showWarning = false }) {
                    Text(stringResource(R.string.root_warning_ack))
                }
            }
        )
    }
}
