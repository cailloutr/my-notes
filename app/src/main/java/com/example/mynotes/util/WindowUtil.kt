package com.example.mynotes.util

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class WindowUtil {

    companion object {
        fun setNoLimitsWindow(activity: AppCompatActivity) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        fun resetWindow(activity: AppCompatActivity) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }
}