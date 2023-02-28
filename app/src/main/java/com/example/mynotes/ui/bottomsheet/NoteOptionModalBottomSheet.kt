package com.example.mynotes.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mynotes.databinding.FragmentNewNoteOptionsBottomSheetBinding

class NoteOptionModalBottomSheet(
    backgroundColor: Int?,
    private var binding: FragmentNewNoteOptionsBottomSheetBinding,
    private val listener: () -> Unit
) : BaseBottomSheet(backgroundColor, binding.root) {

    val TAG = "ModalBottomSheet"
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

        binding.menuBottomSheetCamera.setOnClickListener {
            listener()
            dismiss()
        }
    }
}
