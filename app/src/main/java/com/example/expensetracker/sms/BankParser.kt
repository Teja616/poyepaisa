package com.example.expensetracker.sms


import java.math.BigDecimal

enum class TxnType { DEBIT, CREDIT }

data class ParsedTxn(
    val amountPaise: Long,
    val type: TxnType,
    val bank: String,
    val accountLast4: String,
    val refNo: String?,
    val counterparty: String?
)

object BankParser {

    private const val AMT = """(?:Rs\.?|INR)\s?(?<amount>[\d,]+(?:\.\d{1,2})?)"""
    private const val ACCT = """(?<acct>X+\d{4})"""

    private const val ACCT_STAR = """\*(?<acct>\d{4})"""

    private class Rule(val bank: String, val type: TxnType, val regex: Regex)

    private val rules = listOf(
        // Kotak: "Sent Rs.130.00 from Kotak Bank A/c X2407 to NAME on 09-11-26. UPI Ref 123..."
        // Kotak: "Sent Rs.208.00 from XX9225 to NAME on 08-Nov-26. UPI ref no. 123..."
        Rule(
            "KOTAK", TxnType.DEBIT,
            Regex(
                """Sent\s+$AMT\s+from\s+(?:Kotak Bank A/c\s+)?$ACCT\s+to\s+(?<party>.+?)\s+on\s+[\w-]+\.\s+UPI ref(?: no\.)?\s+(?<ref>\d{12})""",
                RegexOption.IGNORE_CASE
            )
        ),
        // PNB: "A/c X0384 debited INR 60.00 Dt 23-09-26 13:11:44 to NAME thru UPI:123..."
        Rule(
            "PNB", TxnType.DEBIT,
            Regex(
                """A/c\s+$ACCT\s+debited\s+$AMT\s+Dt\s+[\d-]+\s+[\d:]+\s+to\s+(?<party>.+?)\s+thru\s+UPI:(?<ref>\d{12})""",
                RegexOption.IGNORE_CASE
            )
        ),
        // PNB: "A/c X0384 credited for INR 2000.00 on 04-09-26 06:50:16 by NAME thru UPI.AvlBal ...(UPI:123...)"
        Rule(
            "PNB", TxnType.CREDIT,
            Regex(
                """A/c\s+$ACCT\s+credited\s+(?:for\s+)?$AMT\s+on\s+[\d-]+\s+[\d:]+\s+by\s+(?<party>.+?)\s+thru\s+UPI(?:.*?\(UPI:(?<ref>\d{12})\))?""",
                RegexOption.IGNORE_CASE
            )
        ),
        // HDFC: "Sent Rs.1500.00 From HDFC Bank A/C *0123 To NAME On 30/09/26 Ref 890838123553 Not You? ..."
        Rule(
            "HDFC", TxnType.DEBIT,
            Regex(
                """Sent\s+$AMT\s+From\s+HDFC Bank A/C\s+$ACCT_STAR\s+To\s+(?<party>.+?)\s+On\s+[\d/]+\s+Ref\s+(?<ref>\d{12})""",
                RegexOption.IGNORE_CASE
            )
        ),
        // SBI: "Dear UPI user A/C X8350 debited by 10.00 on date 27Oct26 trf to NAME Refno 356360352592 If not u? ..."
        Rule(
            "SBI", TxnType.DEBIT,
            Regex(
                """A/C\s+$ACCT\s+debited\s+by\s+(?<amount>[\d,]+(?:\.\d{1,2})?)\s+on\s+date\s+\w+\s+trf\s+to\s+(?<party>.+?)\s+Refno\s+(?<ref>\d{12})""",
                RegexOption.IGNORE_CASE
            )
        )
    )

    fun parse(body: String): ParsedTxn? {
        val text = body.replace(Regex("\\s+"), " ").trim()
        for (rule in rules) {
            val m = rule.regex.find(text) ?: continue
            val g = m.groups
            return ParsedTxn(
                amountPaise = toPaise(g["amount"]!!.value),
                type = rule.type,
                bank = rule.bank,
                accountLast4 = g["acct"]!!.value.takeLast(4),
                refNo = g["ref"]?.value,
                counterparty = g["party"]?.value?.trim()
            )
        }
        return null
    }

    private fun toPaise(s: String): Long =
        BigDecimal(s.replace(",", "")).movePointRight(2).toLong()
}