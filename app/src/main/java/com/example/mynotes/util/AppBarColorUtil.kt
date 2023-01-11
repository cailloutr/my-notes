package com.example.mynotes.util

import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mynotes.R

class AppBarColorUtil {

    companion object {
        fun changeAppBarColor(activity: AppCompatActivity, it: Int) {
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(it))
        }

        fun resetSystemBarColor(activity: AppCompatActivity) {
            with(activity) {
                this.supportActionBar?.setBackgroundDrawable(
                    ColorDrawable(ContextCompat.getColor(activity, R.color.white))
                )
                this.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
                this.window.navigationBarColor = ContextCompat.getColor(activity, R.color.white)
            }
        }

    }
}