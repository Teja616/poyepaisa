package com.example.expensetracker.sms


import org.junit.Assert.*
import org.junit.Test

class BankParserTest {

    @Test fun kotakWithAccountLabel() {
        val r = BankParser.parse(
            "Sent Rs.130.00 from Kotak Bank A/c X2407 to Jone Doe on 09-11-26. UPI Ref 631315999361. Not done by you? Tap https://kotak.bank.in/KBANKT/Fraud"
        )!!
        assertEquals(13000L, r.amountPaise)
        assertEquals(TxnType.DEBIT, r.type)
        assertEquals("KOTAK", r.bank)
        assertEquals("2407", r.accountLast4)
        assertEquals("631315999361", r.refNo)
        assertEquals("Jone Doe", r.counterparty)
    }

    @Test fun kotakShortForm() {
        val r = BankParser.parse(
            "Sent Rs.208.00 from XX9225 to [REDACTED] on 08-Nov-26. UPI ref no. 667814883854. Not you? Tap https://kotk.in/KOTAKD/InvNIX to report -Kotak"
        )!!
        assertEquals(20800L, r.amountPaise)
        assertEquals("9225", r.accountLast4)
        assertEquals("667814883854", r.refNo)
        assertEquals("[REDACTED]", r.counterparty)
    }

    @Test fun pnbDebit() {
        val r = BankParser.parse(
            "A/c X0384 debited INR 60.00 Dt 23-09-26 13:11:44 to [REDACTED] thru UPI:626613866770.Bal INR 841.01 Not u?Fwd this SMS to 9264092640 to block UPI.-PNB"
        )!!
        assertEquals(6000L, r.amountPaise)
        assertEquals(TxnType.DEBIT, r.type)
        assertEquals("PNB", r.bank)
        assertEquals("0384", r.accountLast4)
        assertEquals("626613866770", r.refNo)
        assertEquals("[REDACTED]", r.counterparty)
    }

    @Test fun pnbCredit() {
        val r = BankParser.parse(
            "A/c X0384 credited for INR 2000.00 on 04-09-26 06:50:16 by [REDACTED] thru UPI.AvlBal INR 10723.01(UPI:661302886625).-PNB"
        )!!
        assertEquals(200000L, r.amountPaise)
        assertEquals(TxnType.CREDIT, r.type)
        assertEquals("661302886625", r.refNo)
        assertEquals("[REDACTED]", r.counterparty)
    }

    @Test fun hdfcMultiLineDebit() {
        val r = BankParser.parse(
            "Sent Rs.1500.00\nFrom HDFC Bank A/C *0123\nTo [REDACTED]\nOn 30/09/26\nRef 890838123553\nNot You?\nCall 18002586161/SMS BLOCK UPI to 7308080808"
        )!!
        assertEquals(150000L, r.amountPaise)
        assertEquals(TxnType.DEBIT, r.type)
        assertEquals("HDFC", r.bank)
        assertEquals("0123", r.accountLast4)
        assertEquals("890838123553", r.refNo)
        assertEquals("[REDACTED]", r.counterparty)
    }

    @Test fun sbiUpiDebit() {
        val r = BankParser.parse(
            "Dear UPI user A/C X8350 debited by 10.00 on date 27Oct26 trf to [REDACTED] Refno 356360352592 If not u? call-1800111109 for other services-18001234-SBI"
        )!!
        assertEquals(1000L, r.amountPaise)
        assertEquals(TxnType.DEBIT, r.type)
        assertEquals("SBI", r.bank)
        assertEquals("8350", r.accountLast4)
        assertEquals("356360352592", r.refNo)
        assertEquals("[REDACTED]", r.counterparty)
    }

    @Test fun commaAmount() {
        val r = BankParser.parse(
            "Sent Rs.1,250.50 from Kotak Bank A/c X2407 to Test on 09-11-26. UPI Ref 123456789012."
        )!!
        assertEquals(125050L, r.amountPaise)
    }

    @Test fun ignoresOtpAndPromo() {
        assertNull(BankParser.parse("123456 is your OTP for txn of Rs.500.00. Do not share."))
        assertNull(BankParser.parse("Get 5% cashback up to Rs.200 on your next payment!"))
    }

    @Test fun axisMerchantPayment() {
        val r = BankParser.parse(
            "INR 172.00 debited\nA/c no. XX1734\n10-11-26, 14:46:54\nUPI/P2M/123456789012/SWIGGY\nNot you? SMS BLOCKUPI Cust ID to 919999999999\nAxis Bank"
        )!!
        assertEquals(17200L, r.amountPaise)
        assertEquals(TxnType.DEBIT, r.type)
        assertEquals("AXIS", r.bank)
        assertEquals("1734", r.accountLast4)
        assertEquals("123456789012", r.refNo)
        assertEquals("SWIGGY", r.counterparty)
    }

    @Test fun axisPersonTransferWithDoubleSpace() {
        val r = BankParser.parse(
            "INR 10.00 debited\nA/c no. XX1734\n19-10-26, 01:08:58\nUPI/P2A/123456789013/JOHN  DOE\nNot you? SMS BLOCKUPI Cust ID to 919999999999\nAxis Bank"
        )!!
        assertEquals(1000L, r.amountPaise)
        assertEquals("123456789013", r.refNo)
        assertEquals("JOHN DOE", r.counterparty)
    }

    @Test fun axisMultiWordMerchant() {
        val r = BankParser.parse(
            "INR 115.00 debited\nA/c no. XX1734\n19-10-26, 22:12:42\nUPI/P2M/123456789014/RAGAM SUPER MARKET\nNot you? SMS BLOCKUPI Cust ID to 919999999999\nAxis Bank"
        )!!
        assertEquals(11500L, r.amountPaise)
        assertEquals("RAGAM SUPER MARKET", r.counterparty)
    }
}