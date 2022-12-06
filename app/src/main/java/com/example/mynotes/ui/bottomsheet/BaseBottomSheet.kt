package com.example.mynotes.ui.bottomsheet

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheet(
    private val backgroundColor: Int?,
    private val binding: View
) : BottomSheetDialogFragment(){

    fun setBackgroundColor() {
        backgroundColor?.let {
            binding.setBackgroundColor(it)
        }
    }
}