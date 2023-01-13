package de.beanfactory.android.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms.Intents
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log

private const val TAG = "SmsReceiver"

private const val OUTGOING_PHONE = "... add your phone here ..."

class SmsReceiver : BroadcastReceiver() {
    private var mLastTimeReceived = 0L

    override fun onReceive(p0: Context?, intent: Intent?) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - mLastTimeReceived > 200) {
            mLastTimeReceived = currentTimeMillis

            val msgFrom: String?
            val msgText: String?
            val msgs = Intents.getMessagesFromIntent(intent) as Array<SmsMessage?>
            msgText = msgs.joinToString(separator = "") {
                it?.displayMessageBody ?: it?.messageBody ?: "-body missing-"
            }
            msgFrom = msgs[0]?.originatingAddress

            val outgoingText = "${msgText} - forward from ${msgFrom} @ ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(msgs[0]?.timestampMillis ?: 0L)}"
            Log.d(TAG, "onReceive: $outgoingText ${msgFrom ?: "no From"}")

            val smsManager = SmsManager.getDefault()
            try {
                val parts = smsManager.divideMessage(outgoingText)

                smsManager.sendMultipartTextMessage(
                    OUTGOING_PHONE,
                    null,
                    parts,
                    null, null
                )
            } catch (e: Throwable) {
                Log.d(TAG, "send: error", e)
            }
        } else {
            Log.d(TAG, "skipped onReceive - received messages too fast")
        }
    }
}