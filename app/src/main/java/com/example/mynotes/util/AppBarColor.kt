package com.example.mynotes.util

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity

class AppBarColor {

    companion object {
        fun changeAppBarColor(activity: AppCompatActivity, it: Int) {
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
        }


    }
}