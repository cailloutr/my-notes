package com.example.mynotes.ui.noteslist

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNotesListBinding
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.ToastUtil
import com.google.android.material.snackbar.Snackbar

class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
    val binding get() = _binding!!

    private val args: NotesListFragmentArgs by navArgs()

    lateinit var hasDeletedANote: NotesListFragmentArgs

    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            (activity?.application as MyNotesApplication)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasDeletedANote = args
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupSnackBarUndoAction(view)
        loadNotesList()
        setupAddNoteButton()
        setupEditTextExpandViewButton()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.note_list_options_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.note_list_options_menu_item_trash) {
                    navigateToTrashFragment()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToTrashFragment() {
        findNavController().navigate(
            NotesListFragmentDirections.actionNotesListFragmentToTrashFragment()
        )
    }

    private fun setupSnackBarUndoAction(view: View) {
//        val hasDeletedANote = args.hasDeletedANote
        if (hasDeletedANote.hasDeletedANote) {
            Snackbar.make(
                view.findViewById(R.id.fragment_notes_button_add_note),
                getString(R.string.note_list_snack_bar_message_moved_to_trash),
                Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.note_list_snack_bar_message_undo)) {
                    undoDeleteNote()
                }
                .show()
        }
    }

    private fun undoDeleteNote() {
        viewModel.retrieveNoteFromTrash()
        viewModel.saveNote()
    }

    private fun loadNotesList() {
        val adapter = setupAdapter()
        viewModel.notesList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun setupEditTextExpandViewButton() {
        binding.fragmentNotesTextInputInsert.setEndIconOnClickListener {
            viewModel.clearNote()
            saveDescriptionInViewModel()
            cleanEditTextInsertNote()
            viewModel.setFragmentMode(FragmentMode.FRAGMENT_NEW)
            viewModel.fragmentMode.value?.let { fragmentMode ->
                navigateToNewNotesFragment(fragmentMode)
            }
        }
    }

    private fun navigateToNewNotesFragment(fragmentMode: FragmentMode) {
        val action =
            NotesListFragmentDirections.actionNotesListFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(action)
    }

    /**
     * Return true if the description field is not empty
     * */
    private fun saveDescriptionInViewModel(): Boolean {
        val description = binding.fragmentNotesTextInputEdittextInsert.text.toString()

        if (description.isNotEmpty()) {
            viewModel.clearNote()
            viewModel.updateViewModelNote(description = description)
            return true
        }

        return false
    }

    private fun setupAddNoteButton() {
        binding.fragmentNotesButtonAddNote.setOnClickListener {

            if (saveDescriptionInViewModel()) {
                viewModel.saveNote()
                cleanEditTextInsertNote()
            } else {
                ToastUtil.makeToast(
                    context,
                    getString(R.string.notes_list_fragment_toast_empty_note)
                )
            }
        }
    }

    private fun setupAdapter(): NotesListAdapter {
        val adapter = NotesListAdapter { note ->
            viewModel.loadNote(note)
            viewModel.setFragmentMode(FragmentMode.FRAGMENT_EDIT)
            viewModel.fragmentMode.value?.let { navigateToNewNotesFragment(it) }
        }
        binding.fragmentNotesRecyclerView.adapter = adapter
        return adapter
    }

    private fun cleanEditTextInsertNote() {
        binding.fragmentNotesTextInputEdittextInsert.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NotesListFragment"
    }
}