package com.example.mynotes.ui.newnote

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.databinding.ColorsOptionsBottomSheetBinding
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.databinding.FragmentNewNoteOptionsBottomSheetBinding
import com.example.mynotes.ui.bottomsheet.NoteOptionModalBottomSheet
import com.example.mynotes.ui.bottomsheet.colors.ColorsOptionBottomSheet
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.AppBarColor
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

        val animation = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
        animation.duration = 200L

        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAppBar()
        setupMenu()

//        if (fragmentMode == FragmentMode.FRAGMENT_NEW) {
//            viewModel.createEmptyNote()
//        }

        loadNoteFromViewModel()
        setupBottomSheet()
    }

    private fun setupBottomSheet() {
        binding.fragmentNewNoteOptionsMenu.setOnClickListener {
            openOptionsBottomSheet()
        }

        binding.fragmentNewNoteOptionsColors.setOnClickListener {
            openColorsOptionBottomSheet()
        }
    }

    private fun openOptionsBottomSheet() {
        val modalBottomSheet = NoteOptionModalBottomSheet(
            viewModel = viewModel,
            backgroundColor = viewModel.note.value?.color,
            binding = FragmentNewNoteOptionsBottomSheetBinding.inflate(
                layoutInflater,
                binding.root,
                false
            )
        )
        modalBottomSheet.show(parentFragmentManager, NoteOptionModalBottomSheet.TAG)
    }

    private fun openColorsOptionBottomSheet() {
        val modalBottomSheet = ColorsOptionBottomSheet(
            backgroundColor = viewModel.note.value?.color, {
                setThemeColors(it)
                viewModel.setNoteColor(it)
            },
            binding = ColorsOptionsBottomSheetBinding.inflate(
                layoutInflater,
                binding.root,
                false
            )
        )
        modalBottomSheet.show(parentFragmentManager, ColorsOptionBottomSheet.TAG)
    }

    private fun setThemeColors(color: Int?) {
        val colorId = color ?: ContextCompat.getColor(requireContext(), R.color.white)
        binding.root.setBackgroundColor(colorId)
        AppBarColor.changeAppBarColor(activity as AppCompatActivity, colorId)
        (activity as AppCompatActivity).apply {
            window.statusBarColor = colorId
            window.navigationBarColor = colorId
        }
    }

    private fun setupAppBar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        if (fragmentMode == FragmentMode.FRAGMENT_EDIT) {
            activity?.title = getString(R.string.app_bar_title_edit_note)
        }
    }

    private fun loadNoteFromViewModel() {
        viewModel.note.observe(viewLifecycleOwner) {
            binding.fragmentNewNoteTextInputEdittextTitle.setText(it?.title)
            binding.fragmentNewNoteTextInputEdittextDescription.setText(it?.description)
            binding.fragmentNewNoteDate.text = it?.modifiedDate
            setThemeColors(it?.color)
        }

        if (fragmentMode == FragmentMode.FRAGMENT_TRASH) {
            binding.apply {
                fragmentNewNoteTextInputEdittextTitle.isEnabled = false
                fragmentNewNoteTextInputEdittextDescription.isEnabled = false
                fragmentNewNoteOptionsColors.isEnabled = false
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
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun undoDeleteNote() {
        viewModel.retrieveNoteFromTrash()
        viewModel.saveNote()
    }

    private fun saveNewNote() {
        val title = binding.fragmentNewNoteTextInputEdittextTitle.text
        val description = binding.fragmentNewNoteTextInputEdittextDescription.text

        if (title.isNullOrEmpty() && description.isNullOrEmpty()) {
            ToastUtil.makeToast(
                context,
                getString(R.string.notes_list_fragment_toast_empty_note)
            )
        } else {
            viewModel.updateViewModelNote(title.toString(), description.toString())
            viewModel.saveNote()
            viewModel.clearNote()
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