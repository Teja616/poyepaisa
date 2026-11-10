package com.example.expensetracker.useri


import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val inr = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))

fun rupees(paise: Long): String = inr.format(paise / 100.0)

fun dateTime(ms: Long): String =
    SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(ms))