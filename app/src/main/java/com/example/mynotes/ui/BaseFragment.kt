package com.example.mynotes.ui

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.example.mynotes.model.Note
import com.example.mynotes.ui.viewModel.NotesListViewModel

open class BaseFragment : Fragment() {

    fun openNote(
        note: Note?,
        viewModel: NotesListViewModel,
        extras: ArrayList<View>,
        navDirections: NavDirections
    ) {
        viewModel.loadNote(note!!)

        val navigatorExtras = FragmentNavigatorExtras(
            extras[0] to "fragment_new_note_title",
            extras[1] to "fragment_new_note_description",
            extras[2] to "fragment_new_note_container"
        )

        navigateWithExtras(navDirections, navigatorExtras)
    }

    private fun navigateWithExtras(
        navDirections: NavDirections,
        navigatorExtras: FragmentNavigator.Extras
    ) {
        findNavController().navigate(
            navDirections,
            navigatorExtras
        )
    }

    fun setupSharedElementsExtras(
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

    fun setupActionModeCallback(
        @MenuRes menuRes: Int,
        onActionItemClickListener: (MenuItem?) -> Boolean,
        onDestroyActionModeListener: () -> Unit
    ): ActionMode.Callback {
        return object : ActionMode.Callback {

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                requireActivity().menuInflater.inflate(menuRes, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return onActionItemClickListener(item)
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                onDestroyActionModeListener()
            }
        }
    }

}