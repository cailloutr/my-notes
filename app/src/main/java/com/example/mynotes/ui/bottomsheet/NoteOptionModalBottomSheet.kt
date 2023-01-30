package com.example.mynotes.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNewNoteOptionsBottomSheetBinding

class NoteOptionModalBottomSheet(
    backgroundColor: Int?,
    private var binding: FragmentNewNoteOptionsBottomSheetBinding,
    private val listener: () -> Unit
) : BaseBottomSheet(backgroundColor, binding.root) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor()

        val deleteOption = view.findViewById<TextView>(R.id.menu_bottom_sheet_colors_label)
        deleteOption.setOnClickListener {
            listener()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
