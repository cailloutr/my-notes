package com.example.mynotes.ui.newnote

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
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.ui.NoteOptionModalBottomSheet
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.ToastUtil


class NewNoteFragment : Fragment() {

    private var _binding: FragmentNewNoteBinding? = null
    val binding get() = _binding!!

    private val args: NewNoteFragmentArgs by navArgs()

    lateinit var fragmentMode: FragmentMode

    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            activity?.application as MyNotesApplication
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentMode = args.fragmentMode
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

        setAppBarTitle()
        setupMenu()
        loadNoteFromViewModel()
        setupOptionsModalBottomSheet()
    }

    private fun setupOptionsModalBottomSheet() {
        binding.fragmentNewNoteOptionsMenu.setOnClickListener {
            val modalBottomSheet = NoteOptionModalBottomSheet(viewModel = viewModel)
            modalBottomSheet.show(parentFragmentManager, NoteOptionModalBottomSheet.TAG)
        }
    }

    private fun setAppBarTitle() {
//        val fragmentMode = args.fragmentMode
        if (fragmentMode == FragmentMode.FRAGMENT_EDIT) {
            activity?.title = getString(R.string.app_bar_title_edit_note)
        }
    }

    private fun loadNoteFromViewModel() {
        viewModel.note.observe(viewLifecycleOwner) {
            binding.fragmentNewNoteTextInputEdittextTitle.setText(it?.title)
            binding.fragmentNewNoteTextInputEdittextDescription.setText(it?.description)
            binding.fragmentNewNoteDate.text = it?.modifiedDate
        }
//        val fragmentMode = args.fragmentMode
        if (fragmentMode == FragmentMode.FRAGMENT_TRASH) {
            binding.apply {
                fragmentNewNoteTextInputEdittextTitle.isEnabled = false
                fragmentNewNoteTextInputEdittextDescription.isEnabled = false
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (args.fragmentMode == FragmentMode.FRAGMENT_TRASH) {
                    menuInflater.inflate(R.menu.fragment_new_note_trash_menu, menu)
                } else {
                    menuInflater.inflate(R.menu.fragment_new_note_menu_save_note, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.fragment_new_note_menu_item_save_note) {
                    saveNewNote()
                }

                if (menuItem.itemId == R.id.fragment_new_note_trash_menu_restore) {
                    undoDeleteNote()
                    navigateToNoteListFragment()
                    // TODO: not let user edit note in trash
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun undoDeleteNote() {
        viewModel.retrieveNoteFromTrash()
        viewModel.saveNote()
    }

    // TODO: EDIT NOTE CRATES A NEW ONE
    private fun saveNewNote() {
        val title = binding.fragmentNewNoteTextInputEdittextTitle.text
        val description = binding.fragmentNewNoteTextInputEdittextDescription.text

        if (title.isNullOrEmpty() && description.isNullOrEmpty()) {
            ToastUtil.makeToast(
                context,
                getString(R.string.notes_list_fragment_toast_empty_note)
            )
        } else {
            viewModel.clearNote()
            viewModel.updateViewModelNote(title.toString(), description.toString())
            viewModel.saveNote()
        }

        navigateToNoteListFragment()
    }

    private fun navigateToNoteListFragment() {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment()
        )
    }


    companion object {
        const val TAG = "NewNoteFragment"
    }
}