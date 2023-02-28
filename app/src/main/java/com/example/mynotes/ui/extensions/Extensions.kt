package com.example.mynotes.ui.extensions

import android.widget.ImageView
import coil.load
import com.example.mynotes.R

fun ImageView.loadImage(url: Any?){
    load(url) {
        crossfade(true)
        placeholder(android.R.color.darker_gray)
        error(R.color.red_200)
        allowHardware(false)
    }
}