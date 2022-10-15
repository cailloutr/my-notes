package com.example.mynotes.ui.noteslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentNotesListBinding
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.ui.viewModel.NotesListViewModelFactory
import com.example.mynotes.util.ToastUtil
import com.example.mynotes.ui.enums.FragmentMode

class NotesListFragment : Fragment() {

    private var _binding: FragmentNotesListBinding? = null
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
        _binding = FragmentNotesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = setupAdapter()

        viewModel.notesList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        setupAddNoteButton()
        setupEdittextExpandViewButton()
    }

    private fun setupEdittextExpandViewButton() {
        binding.fragmentNotesTextInputInsert.setEndIconOnClickListener {

            saveDescriptionInViewModel()
            cleanEditTextInsertNote()

            viewModel.setFragmentMode(FragmentMode.FRAGMENT_NEW)
            viewModel.fragmentMode.value?.let { it1 -> navigateToNewNotesFragment(it1) }
        }
    }

    private fun navigateToNewNotesFragment(fragmentMode: FragmentMode) {
        val action =
            NotesListFragmentDirections.actionNotesListFragmentToNewNoteFragment(fragmentMode)
        findNavController().navigate(action)
    }

    private fun saveDescriptionInViewModel() {
        val description = binding.fragmentNotesTextInputEdittextInsert.text.toString()
        viewModel.createNote(description)
    }

    private fun setupAddNoteButton() {
        binding.fragmentNotesButtonAddNote.setOnClickListener {

            val description = binding.fragmentNotesTextInputEdittextInsert.text.toString()

            if (description.isNotEmpty()) {
                viewModel.createNote(description)
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