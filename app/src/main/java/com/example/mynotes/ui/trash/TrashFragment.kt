package com.example.mynotes.ui.trash

import android.os.Bundle
import android.view.*
import androidx.core.util.size
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentTrashBinding
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.noteslist.NotesListAdapter
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupAdapter()
        loadNotesList()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.trash_list_menu_options, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.trash_list_menu_option_clear_trash -> {
                        showClearTrashConfirmationDialog(
                            resources.getString(R.string.trash_list_menu_option_clear_trash),
                            resources.getString(
                                R.string.fragment_trash_confirmation_dialog_clear_trash_message
                            )
                        ) { viewModel.clearTrash() }
                    }
                    R.id.trash_list_menu_option_restore_all -> {
                        viewModel.restoreAllNotesFromTrash()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showClearTrashConfirmationDialog(title: String, message: String, operacao: () -> Unit) {
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
                operacao()
            }
            .show()
    }

    private fun loadNotesList() {
        val adapter = setupAdapter()
        viewModel.trashList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun navigateToNewNotesFragment(fragmentMode: FragmentMode) {
        val action =
            TrashFragmentDirections.actionTrashFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(action)
    }

    private fun setupAdapter(): NotesListAdapter {
        adapter = NotesListAdapter(viewModel.layoutMode, { note, itemStateArray ->
            if (note != null) {
                viewModel.loadNote(note)
                viewModel.setFragmentMode(FragmentMode.FRAGMENT_TRASH)
                viewModel.fragmentMode.value?.let { navigateToNewNotesFragment(it) }
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

        }, { listOfItemToDelete ->
            viewModel.deleteSelectedNotes(listOfItemToDelete)
        })
        binding.fragmentTrashRecyclerView.adapter = adapter
        return adapter
    }

    private val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            activity?.menuInflater?.inflate(R.menu.contextual_action_bar, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete -> {
                    showClearTrashConfirmationDialog(
                        title = getString(
                            R.string.trash_fragment_delete_selected_notes_dialog_title
                        ),
                        message = getString(
                            R.string.trash_fragment_delete_selected_notes_dialog_message
                        )
                    ) { adapter.deleteSelectedItems() }
                    true
                }
                R.id.more -> {
                    //Todo: Restore all selected items
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