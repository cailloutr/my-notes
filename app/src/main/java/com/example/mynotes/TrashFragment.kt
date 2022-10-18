package com.example.mynotes

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mynotes.databinding.FragmentTrashBinding
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.noteslist.NotesListAdapter
import com.example.mynotes.ui.noteslist.NotesListFragmentDirections
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory


class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    val binding get() = _binding!!

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
                //TODO: change menu layout
                menuInflater.inflate(R.menu.note_list_options_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.note_list_options_menu_item_trash) {
                    //TODO: setup menu action
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadNotesList() {
        val adapter = setupAdapter()
        viewModel.trashList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun navigateToNewNotesFragment(fragmentMode: FragmentMode) {
        val action =
            NotesListFragmentDirections.actionNotesListFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(action)
    }

    private fun setupAdapter(): NotesListAdapter {
        val adapter = NotesListAdapter { note ->
            viewModel.loadNote(note)
            viewModel.setFragmentMode(FragmentMode.FRAGMENT_EDIT)
            viewModel.fragmentMode.value?.let { navigateToNewNotesFragment(it) }
        }
        binding.fragmentTrashRecyclerView.adapter = adapter
        return adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}