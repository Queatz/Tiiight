package com.queatz.tiiight.managers

import android.content.Intent
import android.net.Uri
import com.queatz.on.On
import com.queatz.tiiight.R

class EmailManager constructor(private val on: On) {
    fun sendFeedback() {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "jacobaferrero@gmail.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Tiiight feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "I have some feedback")
        on<ContextManager>().context.startActivity(Intent.createChooser(emailIntent, on<ContextManager>().context.getString(
            R.string.send_feedback)))
    }
}