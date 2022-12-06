package com.example.mynotes.ui.noteslist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.database.model.Note
import com.example.mynotes.databinding.ItemNoteLinearLayoutBinding
import com.example.mynotes.databinding.ItemNoteStaggeredLayoutBinding
import com.example.mynotes.ui.noteslist.touchhelper.ItemTouchHelperAdapter
import com.example.mynotes.ui.viewModel.NotesListViewModel

class NotesListAdapter(
    private val viewModel: NotesListViewModel,
    private val onItemClickListener: (Note) -> Unit
) : ListAdapter<Note, NotesListAdapter.ViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    abstract class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        abstract fun bind(note: Note)
    }

    class NoteViewHolderLinear(
        private var binding: ItemNoteLinearLayoutBinding
        ) : ViewHolder(binding.root) {

        override fun bind(note: Note) {
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate

            val color = note.color ?: R.color.white
            binding.root.setBackgroundColor(color)
        }
    }

    class NoteViewHolderStaggeredGrid(
        private var binding: ItemNoteStaggeredLayoutBinding,
    ) : ViewHolder(binding.root) {

        override fun bind(note: Note) {
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate

            val color = note.color ?: R.color.white
            binding.root.setBackgroundColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewModel.isGridLayout) {
            NoteViewHolderStaggeredGrid(
                ItemNoteStaggeredLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            NoteViewHolderLinear(
                ItemNoteLinearLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)

        holder.itemView.setOnClickListener {
            onItemClickListener(note)
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        //viewModel.moveNoteUpList(fromPosition.toLong(), toPosition.toLong())
        notifyItemMoved(fromPosition, toPosition)
        Log.i("NotesListAdapter", "Current List: ${getCurrentListPositions()}")
    }

    private fun getCurrentListPositions(): String {
        var msg = ""
        repeat(currentList.size) {
            msg += "${currentList[it].position}, "
        }
        return msg
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }
}

