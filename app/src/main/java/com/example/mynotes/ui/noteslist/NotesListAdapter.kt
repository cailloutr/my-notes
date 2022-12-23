package com.example.mynotes.ui.noteslist

import android.content.Context
import android.os.Build
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.util.contains
import androidx.core.util.forEach
import androidx.core.util.remove
import androidx.core.util.size
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.database.model.Note
import com.example.mynotes.databinding.ItemNoteLinearLayoutBinding
import com.example.mynotes.databinding.ItemNoteStaggeredLayoutBinding
import com.example.mynotes.ui.enums.LayoutMode

class NotesListAdapter(
    private val layoutMode: LayoutMode,
    private val onItemClickToSelectListener: (Note?, SparseBooleanArray) -> Unit,
    private val SelectedItemsActionListener: (List<Note>) -> Unit
) : ListAdapter<Note, NotesListAdapter.ViewHolder>(DiffCallback) {

    private val TAG: String = "NoteListAdapter"
    private val itemStateArray = SparseBooleanArray()
    private var isSelectedMode = false

    open class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        open fun bind(note: Note, isSelected: Boolean) {}

        fun setBackgroundColor(note: Note, context: Context) {
            if (note.color != null) {
                (itemView as CardView).setCardBackgroundColor(note.color!!)
            } else {
                (itemView as CardView).setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.white)
                )
            }
        }
    }

    class NoteViewHolderLinear(
        private val binding: ItemNoteLinearLayoutBinding,
        private val context: Context,
        ) : ViewHolder(binding.root) {

        override fun bind(note: Note, isSelected: Boolean) {
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate
            binding.root.isSelected = isSelected

            setBackgroundColor(note, context)
        }
    }

    class NoteViewHolderStaggeredGrid(
        private val binding: ItemNoteStaggeredLayoutBinding,
        private val context: Context,
    ) : ViewHolder(binding.root) {

        override fun bind(note: Note, isSelected: Boolean) {
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate
            binding.root.isSelected = isSelected

            setBackgroundColor(note, context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (layoutMode == LayoutMode.STAGGERED_GRID_LAYOUT) {
            NoteViewHolderStaggeredGrid(
                ItemNoteStaggeredLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                parent.context
            )
        } else {
            NoteViewHolderLinear(
                ItemNoteLinearLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                parent.context
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, itemStateArray[holder.adapterPosition, false])

        holder.itemView.setOnLongClickListener {
            if (!isSelectedMode) {
                if (itemStateArray.contains(holder.adapterPosition)) {
                    itemStateArray.remove(holder.adapterPosition, false)
                } else {
                    itemStateArray.put(holder.adapterPosition, true)
                }
                changeViewState(it)

                onItemClickToSelectListener(null, itemStateArray)
            }

            isSelectedMode = true
            Log.i(TAG, "onBindViewHolder: itemStateArray: $itemStateArray")
            true
        }

        holder.itemView.setOnClickListener {
            if (isSelectedMode) {
                if (itemStateArray.contains(holder.adapterPosition)) {
                    itemStateArray.remove(holder.adapterPosition, true)
                } else {
                    itemStateArray.put(holder.adapterPosition, true)
                }
                changeViewState(it)

                if (itemStateArray.size == 0) {
                    isSelectedMode = false
                }

                onItemClickToSelectListener(null, itemStateArray)
                Log.i(TAG, "onBindViewHolder: itemStateArray: $itemStateArray")
            } else {
                onItemClickToSelectListener(note, itemStateArray)
            }
        }
    }

    private fun changeViewState(view: View) {
        view.isSelected = !view.isSelected
    }

    fun resetStateArray() {
        for (index in itemStateArray.size - 1 downTo 0) {
            val position = itemStateArray.keyAt(index)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                itemStateArray.removeAt(index)
            }
            notifyItemChanged(position)
        }
        isSelectedMode = false
        onItemClickToSelectListener(null, itemStateArray)
    }

    fun deleteSelectedItems(){
        val listOfItemToDelete = mutableListOf<Note>()
        itemStateArray.forEach { key, _ ->
            listOfItemToDelete.add(currentList[key])
        }

        SelectedItemsActionListener(listOfItemToDelete)
        itemStateArray.clear()
        onItemClickToSelectListener(null, itemStateArray)
        isSelectedMode = false
    }

    fun moveSelectedItemsToTrash() {
        val listOfItemToDelete = mutableListOf<Note>()
        itemStateArray.forEach { key, _ ->
            listOfItemToDelete.add(currentList[key])
        }
        SelectedItemsActionListener(listOfItemToDelete)
        itemStateArray.clear()
        onItemClickToSelectListener(null, itemStateArray)
        isSelectedMode = false
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

