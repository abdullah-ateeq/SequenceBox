package com.example.sequencebox.util

import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackHelper {
    fun performLightHaptic(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun performErrorHaptic(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}
