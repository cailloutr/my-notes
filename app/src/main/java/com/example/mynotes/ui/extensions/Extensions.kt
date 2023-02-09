package com.example.mynotes.ui.extensions

import android.widget.ImageView
import coil.load
import com.example.mynotes.R

fun ImageView.loadStartImage(url: Any?, memoryKey: String){
    load(url) {
        crossfade(true)
        placeholder(android.R.color.darker_gray)
        error(R.color.red_200)
        allowHardware(false)
        memoryCacheKey(memoryKey)
    }
}

fun ImageView.loadEndImage(url: Any?, memoryKey: String){
    load(url) {
        crossfade(true)
        placeholder(android.R.color.darker_gray)
        error(R.color.red_200)
        allowHardware(false)
        placeholderMemoryCacheKey(memoryKey)
    }
}

fun ImageView.loadImage(url: Any?){
    load(url) {
        crossfade(true)
        placeholder(android.R.color.darker_gray)
        error(R.color.red_200)
        allowHardware(false)
    }
}