package com.example.mynotes.ui.newnote

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.ToastUtil


class NewNoteFragment : Fragment() {

    private var _binding: FragmentNewNoteBinding? = null
    val binding get() = _binding!!

    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            activity?.application as MyNotesApplication
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragmentNewNoteTextInputEdittextDescription.setText(
            viewModel.newNoteDescription.value
        )
        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_new_note_menu_save_note, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.fragment_new_note_menu_item_save_note) {
                    saveNewNote()
                    clearViewModelNewNoteDescription()
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun clearViewModelNewNoteDescription() {
        viewModel.setNewNoteDescription(null)
    }

    private fun saveNewNote() {
        val title =
            binding.fragmentNewNoteTextInputEdittextTitle.text.toString()
        val description =
            binding.fragmentNewNoteTextInputEdittextDescription.text.toString()

        if (title.isNotEmpty() || description.isNotEmpty()) {
            viewModel.saveNote(
                title = title,
                description = description
            )
        } else {
            ToastUtil.makeToast(
                context,
                getString(R.string.notes_list_fragment_toast_empty_note)
            )
        }

        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment()
        )
    }


    companion object {
        const val TAG = "NewNoteFragment"
    }
}