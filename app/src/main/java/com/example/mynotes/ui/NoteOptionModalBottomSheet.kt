package com.example.mynotes.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.mynotes.R
import com.example.mynotes.ui.newnote.NewNoteFragmentDirections
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// TODO: implement action on Snack bar to undo action delete

class NoteOptionModalBottomSheet(
    private val viewModel: NotesListViewModel
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_new_note_options_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deleteOption = view.findViewById<TextView>(R.id.option_menu_bottom_sheet_delete)
        deleteOption.setOnClickListener {
            viewModel.moveNoteToTrash()
            viewModel.deleteNote()


            Log.d(TAG, "viewModelNote: ${viewModel.note.value}")
            Log.d(TAG, "oviewModelTrash: ${viewModel.noteTrash.value}")
            findNavController().navigate(
                NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment2(true)
            )
            dismiss()
        }
    }



    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
