package com.example.mynotes.ui.bottomsheet.colors

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.mynotes.R
import com.google.android.material.chip.Chip

data class ChipItem(
    val id: Int,
    val text: String,
    @ColorRes val color: Int
) {
    companion object {
        const val TAG = "ChipItemClass"
    }
}

//fun ChipItem.getHexColor(context: Context): String {
//    //.replaceRange(0..1, "3D")
//    return Integer.toHexString(ContextCompat.getColor(context, color))
//}

fun ChipItem.toChip(context: Context, viewGroup: ViewGroup): Chip {

    val chip = LayoutInflater
        .from(context)
        .inflate(R.layout.chip_item, viewGroup, false) as Chip

    val states = arrayOf(
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected),
        intArrayOf(android.R.attr.state_checked, android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_enabled))

    val colors = intArrayOf(
        ContextCompat.getColor(context, color),
        ContextCompat.getColor(context, color),
        ContextCompat.getColor(context, color)
    )

    val strokeColors = intArrayOf(
        ContextCompat.getColor(context, R.color.black),
        ContextCompat.getColor(context, R.color.black_ish),
        ContextCompat.getColor(context, R.color.black_ish)
    )

    val myList = ColorStateList(states, colors)


    chip.chipBackgroundColor = myList
    chip.rippleColor = myList
    chip.chipStrokeWidth = 3f
    chip.chipStrokeColor = ColorStateList(states, strokeColors)
    chip.text = text

    return chip
}