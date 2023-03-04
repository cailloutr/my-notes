package com.example.mynotes.ui.trash

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.size
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentTrashBinding
import com.example.mynotes.model.Note
import com.example.mynotes.ui.BaseFragment
import com.example.mynotes.ui.enums.FragmentMode.FRAGMENT_TRASH
import com.example.mynotes.ui.noteslist.NotesListAdapter
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class TrashFragment : BaseFragment() {

    private var _binding: FragmentTrashBinding? = null
    val binding get() = _binding!!
    private var actionMode: ActionMode? = null
    private lateinit var adapter: NotesListAdapter

/*    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            (activity?.application as MyNotesApplication)
        )
    }*/

    private val viewModel: NotesListViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        setupAppBar()
        setupMenu()
        setupAdapter()
        loadNotesList()
    }

    private fun setupAppBar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.trash_list_menu_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.trash_list_menu_option_clear_trash -> {
                        showConfirmationDialog(
                            resources.getString(R.string.trash_list_menu_option_clear_trash),
                            resources.getString(
                                R.string.fragment_trash_confirmation_dialog_clear_trash_message
                            )
                        ) {
                            viewModel.clearTrash()
                            showSnackBar(getString(R.string.snackbar_message_trash_cleaned))
                        }
                    }
                    R.id.trash_list_menu_option_restore_all -> {
                        viewModel.restoreAllNotesFromTrash()
                        showSnackBar(getString(R.string.snackbar_message_restore_all))
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        operation: () -> Unit
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(
                resources.getString(
                    R.string.clear_trash_dialog_cancel
                )
            ) { _, _ ->
                // Close Dialog
            }
            .setPositiveButton(
                resources.getString(
                    R.string.clear_trash_dialog_confirm
                )
            ) { _, _ ->
                operation()
            }
            .show()
    }

    private fun loadNotesList() {
        val adapter = setupAdapter()
        viewModel.trashList.observe(viewLifecycleOwner) {
            adapter.submitList(it)

            hideEmptyRecyclerView(it)

            // Start the transition once all views have been
            // measured and laid out
            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    private fun hideEmptyRecyclerView(it: List<Note>) {
        if (it.isEmpty()) {
            binding.fragmentTrashRecyclerView.visibility = View.GONE
            binding.fragmentTrashEmptyTrashView.visibility = View.VISIBLE
        } else {
            binding.fragmentTrashRecyclerView.visibility = View.VISIBLE
            binding.fragmentTrashEmptyTrashView.visibility = View.GONE
        }
    }

    private fun setupAdapter(): NotesListAdapter {
        adapter = NotesListAdapter(
            viewModel.layoutMode,
            { note, itemStateArray, title, description, container, image ->
                // Handle the click on a note to open
                if (note != null) {

                    val extras = setupSharedElementsExtras(title, description, container, image)
                    val direction =
                        TrashFragmentDirections.actionTrashFragmentToNewNoteFragment(FRAGMENT_TRASH)

                    openNote(
                        note,
                        viewModel,
                        extras,
                        direction
                    )
                } else {
                    // Handle the selection of items
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
            // handle the deletion of notes
            { listOfSelectedItems, id ->
                val message = if (id == R.id.delete) {
                    viewModel.deleteSelectedNotes(listOfSelectedItems.values.toList())
                    resources.getQuantityString(
                        R.plurals.snackbar_message_notes_deleted,
                        listOfSelectedItems.size
                    )
                } else {
                    viewModel.restoreSelectedNotes(listOfSelectedItems.values.toList())
                    resources.getQuantityString(
                        R.plurals.snackbar_message_notes_restored,
                        listOfSelectedItems.size
                    )
                }
                showSnackBar(message)
            })
        binding.fragmentTrashRecyclerView.adapter = adapter
        return adapter
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private val callback = setupActionModeCallback(
        menuRes = R.menu.trash_fragment_contextual_action_bar,
        onActionItemClickListener = { menuItem ->
            when (menuItem?.itemId) {
                R.id.delete -> {
                    showConfirmationDialog(
                        title = getString(
                            R.string.trash_fragment_delete_selected_notes_dialog_title
                        ),
                        message = getString(
                            R.string.trash_fragment_delete_selected_notes_dialog_message
                        )
                    ) { adapter.returnSelectedItems(menuItem.itemId) }
                    true
                }
                R.id.restore -> {
                    showConfirmationDialog(
                        title = getString(
                            R.string.trash_fragment_restore_selected_notes_dialog_title
                        ),
                        message = getString(
                            R.string.trash_fragment_restore_selected_notes_dialog_message
                        )
                    ) { adapter.returnSelectedItems(menuItem.itemId) }
                    true
                }
                else -> false
            }
        },
        onDestroyActionModeListener = {
            adapter.resetStateArray()
        }
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}