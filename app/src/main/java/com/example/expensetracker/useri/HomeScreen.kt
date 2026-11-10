package com.example.expensetracker.useri


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.AccountTotal
import com.example.expensetracker.data.Transaction

@Composable
fun HomeScreen(vm: AppViewModel, smsGranted: Boolean, requestSms: () -> Unit) {
    val totals by vm.totals.collectAsState()
    val recent by vm.recent.collectAsState()
    val spent = totals.sumOf { it.debitPaise }
    val received = totals.sumOf { it.creditPaise }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!smsGranted) item { PermissionCard(requestSms) }
        item { SummaryCard(spent, received) }
        if (totals.isNotEmpty()) {
            item { SectionTitle("Accounts") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(totals) { AccountCard(it) }
                }
            }
        }
        item { SectionTitle("Recent") }
        if (recent.isEmpty()) {
            item {
                Text(
                    "No transactions yet. Bank SMS will show up here automatically.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(recent, key = { it.id }) { TransactionRow(it) }
    }
}

@Composable
private fun SectionTitle(text: String) =
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

@Composable
private fun SummaryCard(spent: Long, received: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Spent this month", style = MaterialTheme.typography.labelLarge)
            Text(rupees(spent), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Received ${rupees(received)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AccountCard(t: AccountTotal) {
    ElevatedCard(Modifier.width(160.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("${t.bank} ••${t.accountLast4}", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(rupees(t.debitPaise), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("SMS access needed", fontWeight = FontWeight.SemiBold)
            Text("The app reads only bank debit/credit messages, on your device. Nothing leaves your phone.")
            Button(onClick = onGrant) { Text("Grant access") }
        }
    }
}

@Composable
fun TransactionRow(t: Transaction) {
    val debit = t.type == "DEBIT"
    ListItem(
        headlineContent = {
            Text(t.counterparty ?: "Unknown", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = { Text("${t.bank} ••${t.accountLast4} · ${dateTime(t.timestamp)}") },
        trailingContent = {
            Text(
                (if (debit) "−" else "+") + rupees(t.amountPaise),
                fontWeight = FontWeight.SemiBold,
                color = if (debit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
            )
        }
    )
}