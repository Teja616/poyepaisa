package com.example.expensetracker.useri


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TransactionsScreen(vm: AppViewModel) {
    val all by vm.all.collectAsState()
    var selected by remember { mutableStateOf<String?>(null) }

    val accounts = remember(all) { all.map { "${it.bank} ••${it.accountLast4}" }.distinct() }
    val shown = if (selected == null) all
    else all.filter { "${it.bank} ••${it.accountLast4}" == selected }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(
            "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { FilterChip(selected == null, { selected = null }, { Text("All") }) }
            items(accounts) { a -> FilterChip(selected == a, { selected = a }, { Text(a) }) }
        }
        LazyColumn(Modifier.weight(1f)) {
            items(shown, key = { it.id }) { TransactionRow(it) }
        }
    }
}