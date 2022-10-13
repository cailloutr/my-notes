package com.example.mynotes

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.ui.notesList.NotesListViewModel
import com.example.mynotes.ui.notesList.NotesListViewModelFactory
import com.example.mynotes.util.DateUtil.Companion.getFormattedDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NewNoteFragment : Fragment() {


    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentNewNoteBinding? = null
    val binding get() = _binding!!

    private val viewModel: NotesListViewModel by viewModels {
        NotesListViewModelFactory(
            activity?.application as MyNotesApplication
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        setupMenu()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_new_note_menu_save_note, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                if (menuItem.itemId == R.id.fragment_new_note_menu_item_save_note) {
                    saveNewNote()
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveNewNote() {
        val title =
            binding.fragmentNewNoteTextInputEdittextTitle.text.toString()
        val description =
            binding.fragmentNewNoteTextInputEdittextDescription.text.toString()

        viewModel.saveNote(
            title = title,
            description = description
        )

        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment()
        )
    }


    companion object {
        const val TAG = "NewNoteFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewNoteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewNoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}