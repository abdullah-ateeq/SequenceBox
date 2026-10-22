package com.example.sequencebox.util

import android.content.Context
import android.content.Intent

object ShareHelper {
    fun shareText(context: Context, text: String, title: String = "Share Sequence") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }
}
