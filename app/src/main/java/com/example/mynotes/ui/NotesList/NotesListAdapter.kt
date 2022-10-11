package com.example.mynotes.ui.NotesList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.mynotes.database.model.Note
import com.example.mynotes.databinding.ItemNoteBinding

class NotesListAdapter(
    private val onItemClickListener: (Note) -> Unit
) : ListAdapter<Note, NotesListAdapter.NoteViewHolder>(DiffCallback) {

    class NoteViewHolder(private var binding: ItemNoteBinding): ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedData
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
            )

//        viewHolder.itemView.setOnClickListener {
//            val position = viewHolder.adapterPosition
//            onItemClicked(getItem(position))

        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
      holder.bind(getItem(position))
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

