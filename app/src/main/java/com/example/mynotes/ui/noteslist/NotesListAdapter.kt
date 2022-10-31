package com.example.mynotes.ui.noteslist

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mynotes.database.model.Note
import com.example.mynotes.databinding.ItemNoteBinding
import com.example.mynotes.ui.noteslist.touchhelper.ItemTouchHelperAdapter
import com.example.mynotes.ui.viewModel.NotesListViewModel

class NotesListAdapter(
    private val viewModel: NotesListViewModel,
    private val onItemClickListener: (Note) -> Unit
) : ListAdapter<Note, NotesListAdapter.NoteViewHolder>(DiffCallback), ItemTouchHelperAdapter {

    class NoteViewHolder(private var binding: ItemNoteBinding) : ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
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

    fun getCurrentListPositions(): String {
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

