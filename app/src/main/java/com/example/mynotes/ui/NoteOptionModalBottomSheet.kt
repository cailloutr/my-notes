package com.example.mynotes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mynotes.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NoteOptionModalBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View?{

        return inflater.inflate(R.layout.fragment_new_note_options_bottom_sheet, container, false)
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
