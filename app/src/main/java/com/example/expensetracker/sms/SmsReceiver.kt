package com.example.expensetracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.expensetracker.data.AppDatabase
import com.example.expensetracker.data.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    // Temporary allowlist; we'll replace with your real sender IDs
    private val allowedSenders = listOf("KOTAK", "PNB", "HDFC","SBI","AXIS")
    private val filterSenders = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isNullOrEmpty()) return

        val sender = parts[0].originatingAddress ?: return
        Log.d("SmsReceiver", "sender=$sender")
        if (filterSenders && allowedSenders.none { sender.contains(it, ignoreCase = true) }) return

        // Long SMS arrive in several parts; join them
        val body = parts.joinToString("") { it.messageBody ?: "" }
        val time = parts[0].timestampMillis

        val parsed = BankParser.parse(body)
        if (parsed == null) {
            Log.w("SmsReceiver", "Unparsed bank SMS from $sender: $body")
            return
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.get(context).dao().insert(
                    Transaction(
                        amountPaise = parsed.amountPaise,
                        type = parsed.type.name,
                        bank = parsed.bank,
                        accountLast4 = parsed.accountLast4,
                        refNo = parsed.refNo,
                        counterparty = parsed.counterparty,
                        timestamp = time,
                        rawSms = body
                    )
                )
            } finally {
                pending.finish()
            }
        }
    }
}