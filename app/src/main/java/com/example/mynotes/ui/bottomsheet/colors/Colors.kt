package com.example.mynotes.ui.bottomsheet.colors

import com.example.mynotes.R

class Colors() {

    companion object {
        val listOfColorsChip = listOf(
            ChipItem(1, "White", R.color.white, true),
            ChipItem(2, "Red", R.color.chip_red, false),
            ChipItem(3, "Blue", R.color.chip_blue, false),
            ChipItem(4, "Green", R.color.chip_green,false),
            ChipItem(5, "Yellow", R.color.chip_yellow, false),
            ChipItem(6, "Pink", R.color.chip_pink, false)
        )
    }
}