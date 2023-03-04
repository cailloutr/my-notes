package com.example.mynotes.ui.noteslist

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.util.size
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNotesListBinding
import com.example.mynotes.model.Note
import com.example.mynotes.ui.BaseFragment
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.enums.FragmentMode.FRAGMENT_EDIT
import com.example.mynotes.ui.enums.FragmentMode.FRAGMENT_NEW
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.util.ToastUtil
import com.example.mynotes.util.windowinsets.InsetsWithKeyboardAnimationCallback
import com.example.mynotes.util.windowinsets.InsetsWithKeyboardCallback
import com.example.mynotes.util.windowinsets.WindowUtil.Companion.setupEdgeToEdgeLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.activityViewModel

// TODO: Other options like add checkBoxes for each note as a shopping list
// TODO: Add markers like topics
// TODO: Share option
// TODO: Archive item on swipe
// TODO: auto clear the trash
// TODO: reminders
// TODO: favorites

private const val TAG: String = "NoteListFragment"
private const val STAGGERED_GRID_SPAN_COUNT = 2

class NotesListFragment : BaseFragment() {
    private lateinit var adapter: NotesListAdapter

    private var _binding: FragmentNotesListBinding? = null
    val binding get() = _binding!!

    private var actionMode: ActionMode? = null

    private val args: NotesListFragmentArgs by navArgs()

    private var hasRemovedNote: Boolean = false

/*    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            (activity?.application as MyNotesApplication)
        )
    }*/

    private val viewModel: NotesListViewModel by activityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasRemovedNote = args.hasRemovedNote
        resetViewModelNoteState()
        getLayoutModeFromDataStore()
    }

    private fun getLayoutModeFromDataStore() {
        runBlocking {
            val layoutMode = async { viewModel.userPreferencesFlow.first().layoutMode }
            viewModel.setLayoutMode(LayoutMode.valueOf(layoutMode.await()))
        }
    }

    private fun resetViewModelNoteState() {
        viewModel.updateViewModelNoteHasImage(false)
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

        setupEdgeToEdgeLayout(
            window = requireActivity().window,
            toolbar = binding.toolbar,
            footer = binding.fragmentNotesListFooter
        )

        moveFooterWithKeyboard()
        postponeEnterTransition()
        setupMenu()
        chooseLayout()
        setupAddNoteButton()
        setupEditTextExpandViewButton()


    }

    private fun moveFooterWithKeyboard() {
        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(requireActivity().window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(binding.root, insetsWithKeyboardCallback)

        val insetsWithKeyboardAnimationCallback =
            InsetsWithKeyboardAnimationCallback(binding.fragmentNotesListFooter)
        ViewCompat.setWindowInsetsAnimationCallback(
            binding.fragmentNotesListFooter,
            insetsWithKeyboardAnimationCallback
        )
    }

    private fun setupAppBar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.note_list_options_menu, menu)

                val layoutButton = menu.findItem(R.id.note_list_options_menu_layout_style)
                setLayoutModeIcon(layoutButton)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.note_list_options_menu_item_trash) {
                    navigateToTrashFragment()
                }

                if (menuItem.itemId == R.id.note_list_options_menu_layout_style) {
                    changeLayoutMode(menuItem)
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun changeLayoutMode(menuItem: MenuItem) {
        viewModel.setLayoutMode(
            if (viewModel.layoutMode == LayoutMode.STAGGERED_GRID_LAYOUT) {
                LayoutMode.LINEAR_LAYOUT
            } else {
                LayoutMode.STAGGERED_GRID_LAYOUT
            }
        )
        viewModel.hasPreferencesChanged(true)
        setLayoutModeIcon(menuItem)
        chooseLayout()
    }

    private fun chooseLayout() {
        when (viewModel.layoutMode) {
            LayoutMode.STAGGERED_GRID_LAYOUT -> {
                binding.fragmentNotesRecyclerView.layoutManager =
                    StaggeredGridLayoutManager(
                        STAGGERED_GRID_SPAN_COUNT,
                        StaggeredGridLayoutManager.VERTICAL
                    )
            }

            LayoutMode.LINEAR_LAYOUT -> {
                binding.fragmentNotesRecyclerView.layoutManager =
                    LinearLayoutManager(context)
            }
        }
        loadNotesList()
    }

    private fun setLayoutModeIcon(menuItem: MenuItem?) {
        if (menuItem == null) return

        if (viewModel.layoutMode.value) {
            menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_view_list)
            menuItem.title = getString(R.string.note_list_options_menu_layout_linear)
        } else {
            menuItem.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_grid_view)
            menuItem.title = getString(R.string.note_list_options_menu_layout_grid)
        }
    }

    private fun loadNotesList() {
        val adapter = setupAdapter()
        viewModel.notesList.observe(viewLifecycleOwner) { noteList ->
            adapter.submitList(noteList)

            // Start the transition once all views have been
            // measured and laid out
            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }

            /**
             * If hasRemovedNote == true means that a delete action had been performed in NewNoteFragment
             * Then it gets a reference from the deleted note from the viewModel and show the snack-bar that
             * will only delete it from the database if the undo action is not called
             * */
            if (hasRemovedNote) {
                val noteMap = mutableMapOf<Int, Note>()
                viewModel.note.value?.let {
                    val index = adapter.currentList.indexOf(it)
                    noteMap.put(index, it)
                }

                showSnackbarWithUndoDeleteAction(noteMap) {
                    hasRemovedNote = false
                }
            }
        }
        Log.i(TAG, "loadNotesList: ${adapter.currentList}")
    }

    private fun showSnackbarWithUndoDeleteAction(
        noteMap: MutableMap<Int, Note>,
        operation: () -> Unit = {}
    ) {
        var undoAction = false
        removeNotesFromCurrentList(noteMap)

        val snackbar = Snackbar.make(
            binding.fragmentNotesCardviewButtonAddNote,
            getString(R.string.note_list_fragment_move_to_trash_snackbar),
            Snackbar.LENGTH_SHORT
        ).setAction(R.string.note_list_snack_bar_message_undo) {
            undoRemoveAction(noteMap)
            undoAction = true
        }.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)

                if (!undoAction) {
                    viewModel.moveSelectedItemsToTrash(noteMap.values.toList())
                }

                undoAction = false
                operation()
            }
        })
        snackbar.show()
    }

    private fun setupEditTextExpandViewButton() {
        binding.fragmentNotesTextInputInsert.setEndIconOnClickListener {
            viewModel.createEmptyNote()
            saveDescriptionInViewModel()
            cleanEditTextInsertNote()

            navigateToNewNotesFragment(FRAGMENT_NEW)
        }
    }

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
            { note, itemStateArray, title, description, container, image ->
                // Handles a single click to open a note
                if (note != null) {

                    val extras = setupSharedElementsExtras(title, description, container, image)
                    val direction =
                        NotesListFragmentDirections.actionNotesListFragmentToNewNoteFragment(
                            FRAGMENT_EDIT
                        )

                    openNote(
                        note,
                        viewModel,
                        extras,
                        direction
                    )
                } else {
                    // Handle the selection of multiples items
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
            // Handles the action delete on the selected items
            { listOfItems: Map<Int, Note>, _ ->
                showSnackbarWithUndoDeleteAction(listOfItems as MutableMap<Int, Note>)
            }
        )

        binding.fragmentNotesRecyclerView.adapter = adapter
        return adapter
    }


    private fun undoRemoveAction(
        listOfItems: Map<Int, Note>
    ) {
        val mutableList = adapter.currentList.toMutableList()
        listOfItems.forEach {
            mutableList.add(it.key, it.value)
        }
        adapter.submitList(mutableList)
    }

    private fun removeNotesFromCurrentList(listOfItems: Map<Int, Note>) {
        val mutableList = adapter.currentList.toMutableList()
        mutableList.removeAll(listOfItems.values)
        adapter.submitList(mutableList)
    }

    private val callback = setupActionModeCallback(
        menuRes = R.menu.notes_list_fragment_contextual_action_bar,
        onActionItemClickListener = { menuItem ->
            when (menuItem?.itemId) {
                R.id.delete -> {
                    adapter.returnSelectedItems(null)
                    true
                }
                else -> false
            }
        },
        onDestroyActionModeListener = {
            adapter.resetStateArray()
        }
    )

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

    override fun onStop() {
        super.onStop()
        savePreferencesInDataStore()
    }

    private fun savePreferencesInDataStore() {
        if (viewModel.hasPreferencesChanged) {
            viewModel.updateLayoutMode()
            viewModel.hasPreferencesChanged(false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}