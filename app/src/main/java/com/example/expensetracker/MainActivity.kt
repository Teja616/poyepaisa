package com.example.expensetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.expensetracker.useri.AppRoot
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private var smsGranted by mutableStateOf(false)

    private val askSms = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { smsGranted = hasSmsPermission() }

    private fun hasSmsPermission() = listOf(
        Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS
    ).all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    override fun onResume() {
        super.onResume()
        smsGranted = hasSmsPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpenseTrackerTheme {
                AppRoot(
                    smsGranted = smsGranted,
                    requestSms = {
                        askSms.launch(
                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                        )
                    }
                )
            }
        }
    }
}