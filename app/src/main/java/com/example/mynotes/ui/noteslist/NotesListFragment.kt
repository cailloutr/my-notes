package com.example.mynotes.ui.noteslist

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.util.size
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNotesListBinding
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.AppBarColorUtil.Companion.resetSystemBarColor
import com.example.mynotes.util.ToastUtil
import com.example.mynotes.util.WindowUtil
import com.google.android.material.snackbar.Snackbar

// TODO: Coordinator layout
// TODO: Image notes - Take a photo or choose from the gallery
// TODO: Other easy options like add checkBoxes for each note as a shopping list
// TODO: Add markers like topics
// TODO: Share option
// TODO: Archive item on swipe
// TODO: Implement design improvements
// TODO: fix undo action behavior and Snackbar
// TODO: auto clear the trash

class NotesListFragment : Fragment() {
    private val TAG: String = "NoteListFragment"
    private lateinit var adapter: NotesListAdapter
    private var _binding: FragmentNotesListBinding? = null
    val binding get() = _binding!!
    private val args: NotesListFragmentArgs by navArgs()

    lateinit var hasDeletedANote: NotesListFragmentArgs
    lateinit var sharedPref: SharedPreferences
    private var actionMode: ActionMode? = null

    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            (activity?.application as MyNotesApplication)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) as SharedPreferences

        viewModel.layoutMode.value =
            sharedPref.getBoolean(getString(R.string.pref_key_layout_manager), false)

        hasDeletedANote = args
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setupAppBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        WindowUtil.resetWindow(requireActivity() as AppCompatActivity)

        postponeEnterTransition()
        setupMenu()
        setupSnackBarUndoAction(view)
        chooseLayout()
        setupAddNoteButton()
        setupEditTextExpandViewButton()
    }

    override fun onResume() {
        super.onResume()
        resetSystemBarColor(activity as AppCompatActivity)
    }

    private fun setupAppBar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun chooseLayout() {
        when (viewModel.layoutMode) {
            LayoutMode.STAGGERED_GRID_LAYOUT -> {
                binding.fragmentNotesRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                setupAdapter()
                loadNotesList()
            }

            LayoutMode.LINEAR_LAYOUT -> {
                binding.fragmentNotesRecyclerView.layoutManager =
                    LinearLayoutManager(context)
                setupAdapter()
                loadNotesList()
            }
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.note_list_options_menu, menu)

                val layoutButton = menu.findItem(R.id.note_list_options_menu_layout_style)
                setIcon(layoutButton)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.note_list_options_menu_item_trash) {
                    navigateToTrashFragment()
                }

                if (menuItem.itemId == R.id.note_list_options_menu_layout_style) {
                    viewModel.layoutMode =
                        if (viewModel.layoutMode == LayoutMode.STAGGERED_GRID_LAYOUT) {
                            LayoutMode.LINEAR_LAYOUT
                        } else {
                            LayoutMode.STAGGERED_GRID_LAYOUT
                        }
                    chooseLayout()
                    setIcon(menuItem)

                    saveOptionInSharedPreferences()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveOptionInSharedPreferences() {
        with(sharedPref.edit()) {
            this?.putBoolean(
                getString(R.string.pref_key_layout_manager),
                viewModel.layoutMode.value
            )
            this?.apply()
        }
    }

    private fun setIcon(menuItem: MenuItem?) {
        if (menuItem == null) return

        if (viewModel.layoutMode.value) {
            menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_view_list)
            menuItem.title = getString(R.string.note_list_options_menu_layout_linear)
        } else {
            menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid_view)
            menuItem.title = getString(R.string.note_list_options_menu_layout_grid)
        }
    }

    private fun navigateToTrashFragment() {
        findNavController().navigate(
            NotesListFragmentDirections.actionNotesListFragmentToTrashFragment()
        )
    }

    private fun navigateToNewNotesFragment(fragmentMode: FragmentMode) {
        val action =
            NotesListFragmentDirections.actionNotesListFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(action)
    }

    private fun navigateToNewNotesFragmentExtras(
        fragmentMode: FragmentMode,
        extrasArray: ArrayList<View>
    ) {
        val navigatorExtras = FragmentNavigatorExtras(
            extrasArray[0] to "fragment_new_note_title",
            extrasArray[1] to "fragment_new_note_description",
            extrasArray[2] to "fragment_new_note_container"
        )

        val action =
            NotesListFragmentDirections.actionNotesListFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(
            action,
            navigatorExtras
        )
    }



    //TODO: fix undo action
    private fun setupSnackBarUndoAction(view: View) {
//        val hasDeletedANote = args.hasDeletedANote
        if (hasDeletedANote.hasDeletedANote) {
            Snackbar.make(
                binding.fragmentNotesCardviewButtonAddNote,
                getString(R.string.note_list_snack_bar_message_moved_to_trash),
                Snackbar.LENGTH_LONG
            )
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

            // Start the transition once all views have been
            // measured and laid out
            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    private fun setupEditTextExpandViewButton() {
        binding.fragmentNotesTextInputInsert.setEndIconOnClickListener {
            viewModel.createEmptyNote()
            saveDescriptionInViewModel()
            cleanEditTextInsertNote()
            viewModel.setFragmentMode(FragmentMode.FRAGMENT_NEW)
            viewModel.fragmentMode.value?.let { fragmentMode ->
                navigateToNewNotesFragment(fragmentMode)
            }
        }
    }

    /**
     * Return true if the description field is not empty
     * */
    private fun saveDescriptionInViewModel(): Boolean {
        val description = binding.fragmentNotesTextInputEdittextInsert.text.toString()

        if (description.isNotEmpty()) {
            viewModel.updateViewModelNote(description = description)
            return true
        }

        return false
    }

    private fun setupAddNoteButton() {
        binding.fragmentNotesButtonAddNote.setOnClickListener {
            viewModel.createEmptyNote()
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

    private fun cleanEditTextInsertNote() {
        binding.fragmentNotesTextInputEdittextInsert.text?.clear()
    }

    private fun setupAdapter(): NotesListAdapter {
        adapter = NotesListAdapter(
            viewModel.layoutMode,
            { note, itemStateArray, title, description, container ->
                if (note != null) {
                    viewModel.loadNote(note)
                    viewModel.setFragmentMode(FragmentMode.FRAGMENT_EDIT)

                    val extras = arrayListOf<View>()
                    if (title != null) {
                        extras.add(title)
                    }
                    if (description != null) {
                        extras.add(description)
                    }
                    if (container != null) {
                        extras.add(container)
                    }
                    viewModel.fragmentMode.value?.let {
                        navigateToNewNotesFragmentExtras(it, extras)
                    }
                } else {
                    when (itemStateArray.size) {
                        1 -> {
                            if (actionMode == null) {
                                actionMode = activity?.startActionMode(callback)
                            }
                            actionMode?.title = itemStateArray.size.toString()
                        }
                        0 -> {
                            actionMode?.finish()
                            actionMode = null
                        }
                        else -> {
                            actionMode?.title = itemStateArray.size.toString()
                        }
                    }
                }
            },
            { listOfItems, _ ->
                viewModel.moveSelectedItemsToTrash(listOfItems)
                Snackbar.make(
                    binding.fragmentNotesCardviewButtonAddNote,
                    getString(R.string.note_list_fragment_move_to_trash_snackbar),
                    Snackbar.LENGTH_SHORT
                ).show()
            })
        binding.fragmentNotesRecyclerView.adapter = adapter

        return adapter
    }

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            activity?.menuInflater?.inflate(R.menu.notes_list_fragment_contextual_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete -> {
                    adapter.returnSelectedItems(null)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.resetStateArray()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}