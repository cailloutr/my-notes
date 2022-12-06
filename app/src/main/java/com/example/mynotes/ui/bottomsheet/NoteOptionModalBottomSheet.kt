package com.example.mynotes.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNewNoteOptionsBottomSheetBinding
import com.example.mynotes.ui.newnote.NewNoteFragmentDirections
import com.example.mynotes.ui.viewModel.NotesListViewModel


class NoteOptionModalBottomSheet(
    backgroundColor: Int?,
    private val viewModel: NotesListViewModel,
    private var binding: FragmentNewNoteOptionsBottomSheetBinding
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
            if (viewModel.note.value?.isTrash == true) {
                viewModel.deleteNote()
                navigateToTrashFragment()
            } else {
                viewModel.moveNoteToTrash()
                viewModel.saveNote()
                navigateToNoteListFragment()
            }
            dismiss()
        }
    }

    private fun navigateToTrashFragment() {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToTrashFragment()
        )
    }

    private fun navigateToNoteListFragment() {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment(true)
        )
    }


    companion object {
        const val TAG = "ModalBottomSheet"
    }
}
