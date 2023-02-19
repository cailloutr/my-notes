package com.example.mynotes.ui.noteslist

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.util.size
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mynotes.R
import com.example.mynotes.database.model.Note
import com.example.mynotes.databinding.FragmentNotesListBinding
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.util.ToastUtil
import com.example.mynotes.util.windowinsets.InsetsWithKeyboardAnimationCallback
import com.example.mynotes.util.windowinsets.InsetsWithKeyboardCallback
import com.example.mynotes.util.windowinsets.WindowUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.activityViewModel

// TODO: Image notes - Take a photo
// TODO: Other options like add checkBoxes for each note as a shopping list
// TODO: Add markers like topics
// TODO: Share option
// TODO: Archive item on swipe
// TODO: auto clear the trash
// TODO: lembretes

//private const val TAG: String = "NoteListFragment"

class NotesListFragment : Fragment() {
    private lateinit var adapter: NotesListAdapter
    private var _binding: FragmentNotesListBinding? = null
    val binding get() = _binding!!

    private var actionMode: ActionMode? = null

/*    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            (activity?.application as MyNotesApplication)
        )
    }*/

    private val viewModel: NotesListViewModel by activityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.updateViewModelNoteHasImage(false)
        runBlocking {
            val layoutMode = async { viewModel.userPreferencesFlow.first().layoutMode }
            viewModel.setLayoutMode(LayoutMode.valueOf(layoutMode.await()))
        }
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

        setupEdgeToEdgeLayout()
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

    private fun setupEdgeToEdgeLayout() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowUtil.implementsSystemBarInsets(binding.toolbar, binding.fragmentNotesListFooter)
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
                setLayoutModeIcon(layoutButton)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.note_list_options_menu_item_trash) {
                    navigateToTrashFragment()
                }

                if (menuItem.itemId == R.id.note_list_options_menu_layout_style) {
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
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
            { note, itemStateArray, title, description, container, image ->
                // Handles a single click to open a note
                if (note != null) {
                    viewModel.loadNote(note)
                    viewModel.setFragmentMode(FragmentMode.FRAGMENT_EDIT)

                    val extras = setupSharedElementsExtras(title, description, container, image)

                    viewModel.fragmentMode.value?.let {
                        navigateToNewNotesFragmentExtras(it, extras)
                    }
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
            { listOfItems, _ ->
                var undoAction = false
                removeNotesFromCurrentList(listOfItems)

                val snackbar = Snackbar.make(
                    binding.fragmentNotesCardviewButtonAddNote,
                    getString(R.string.note_list_fragment_move_to_trash_snackbar),
                    Snackbar.LENGTH_SHORT
                ).setAction(R.string.note_list_snack_bar_message_undo) {
                    undoRemoveAction(listOfItems)
                    undoAction = true
                }.addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)

                        if (!undoAction) {
                            viewModel.moveSelectedItemsToTrash(listOfItems.values.toList())
                        }

                        undoAction = false
                    }
                })
                snackbar.show()
            })

        binding.fragmentNotesRecyclerView.adapter = adapter
        return adapter
    }

    private fun setupSharedElementsExtras(
        title: View?,
        description: View?,
        container: View?,
        image: View?
    ): ArrayList<View> {
        val extras: ArrayList<View> = arrayListOf()
        if (title != null) {
            extras.add(title)
        }
        if (description != null) {
            extras.add(description)
        }
        if (container != null) {
            extras.add(container)
        }
        if (image != null) {
            extras.add(image)
        }
        return extras
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

    override fun onStop() {
        super.onStop()
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