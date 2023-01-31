package com.example.mynotes.ui.trash

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.size
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.database.model.Note
import com.example.mynotes.databinding.FragmentTrashBinding
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.noteslist.NotesListAdapter
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.AppBarColorUtil
import com.example.mynotes.util.WindowUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar


class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    val binding get() = _binding!!
    private var actionMode: ActionMode? = null
    private lateinit var adapter: NotesListAdapter

    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            (activity?.application as MyNotesApplication)
        )
    }

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
        WindowUtil.resetWindow(requireActivity() as AppCompatActivity)

        postponeEnterTransition()
        setuptAppBar()
        setupMenu()
        setupAdapter()
        loadNotesList()
    }

    override fun onResume() {
        super.onResume()
        AppBarColorUtil.resetSystemBarColor(activity as AppCompatActivity)
    }

    private fun setuptAppBar() {
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
                        ) { viewModel.clearTrash() }
                        showSnackBar(getString(R.string.snackbar_message_trash_cleaned))
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

            hideEmptyRecyclerVIew(it)

            // Start the transition once all views have been
            // measured and laid out
            (view?.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    private fun hideEmptyRecyclerVIew(it: List<Note>) {
        if (it.isEmpty()) {
            binding.fragmentTrashRecyclerView.visibility = View.GONE
            binding.fragmentTrashEmptyTrashView.visibility = View.VISIBLE
        } else {
            binding.fragmentTrashRecyclerView.visibility = View.VISIBLE
            binding.fragmentTrashEmptyTrashView.visibility = View.GONE
        }
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
            TrashFragmentDirections.actionTrashFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(
            action,
            navigatorExtras
        )
    }

    private fun setupAdapter(): NotesListAdapter {
        adapter = NotesListAdapter(
            viewModel.layoutMode,
            { note, itemStateArray, title, description, container ->
                // Handle the click on a note to open
                if (note != null) {
                    viewModel.loadNote(note)
                    viewModel.setFragmentMode(FragmentMode.FRAGMENT_TRASH)

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

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            activity?.menuInflater?.inflate(R.menu.trash_fragment_contextual_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete -> {
                    showConfirmationDialog(
                        title = getString(
                            R.string.trash_fragment_delete_selected_notes_dialog_title
                        ),
                        message = getString(
                            R.string.trash_fragment_delete_selected_notes_dialog_message
                        )
                    ) { adapter.returnSelectedItems(item.itemId) }
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
                    ) { adapter.returnSelectedItems(item.itemId) }
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