package com.example.expensetracker.useri


import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(smsGranted: Boolean, requestSms: () -> Unit) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        ListItem(
            headlineContent = { Text("SMS access") },
            supportingContent = { Text(if (smsGranted) "Granted" else "Not granted") },
            trailingContent = { if (!smsGranted) Button(onClick = requestSms) { Text("Grant") } }
        )
        ListItem(
            headlineContent = { Text("Battery optimization") },
            supportingContent = { Text("Exempt this app so SMS are tracked reliably") },
            modifier = Modifier.clickable {
                ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        )
        ListItem(
            headlineContent = { Text("App permissions") },
            supportingContent = { Text("Open system settings for this app") },
            modifier = Modifier.clickable {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", ctx.packageName, null))
                )
            }
        )
    }
}