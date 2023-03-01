package com.example.mynotes.ui.noteslist

import android.content.Context
import android.os.Build
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.util.contains
import androidx.core.util.forEach
import androidx.core.util.remove
import androidx.core.util.size
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.R
import com.example.mynotes.model.Note
import com.example.mynotes.databinding.ItemNoteLinearLayoutBinding
import com.example.mynotes.databinding.ItemNoteStaggeredLayoutBinding
import com.example.mynotes.ui.enums.LayoutMode
import com.example.mynotes.ui.extensions.loadImage

//private const val TAG: String = "NoteListAdapter"

class NotesListAdapter(
    private val layoutMode: LayoutMode,
    private val onItemClickToSelectListener: (Note?, SparseBooleanArray, View?, View?, View?, View?) -> Unit,
    private val selectedItemsActionListener: (Map<Int, Note>, Int?) -> Unit,
) : ListAdapter<Note, NotesListAdapter.ViewHolder>(DiffCallback) {

    private val itemStateArray = SparseBooleanArray()

    /**
     * Defines if the user had a long click so the next click will select a new item instead of open a note
     * */
    private var isSelectedMode = false

    open class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        val title: View = view.findViewById(R.id.item_note_title)
        val description: View = view.findViewById(R.id.item_note_description)
        val image: View = view.findViewById(R.id.item_note_image)
        val container: View = view.rootView

        open fun bind(note: Note, isSelected: Boolean) {
            ViewCompat.setTransitionName(
                title, note.id + "_title"
            )
            ViewCompat.setTransitionName(
                description, note.id + "_description"
            )
            ViewCompat.setTransitionName(
                container, note.id + "_container"
            )
            ViewCompat.setTransitionName(
                image, note.id + "_image"
            )
        }

        fun setBackgroundColor(note: Note, context: Context) {
            if (note.color != null) {
                (itemView as CardView).setCardBackgroundColor(note.color!!)
            } else {
                (itemView as CardView).setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.white)
                )
            }
        }

        fun setImage(note: Note) {
            val imageView = itemView.findViewById<ImageView>(R.id.item_note_image)
            imageView.visibility = if (note.hasImage) View.VISIBLE else View.GONE

            if (!note.imageUrl.isNullOrEmpty()) {
                imageView.apply {
                    loadImage(note.imageUrl)
                }
            }
        }
    }

    class NoteViewHolderLinear(
        private val binding: ItemNoteLinearLayoutBinding,
        private val context: Context,
    ) : ViewHolder(binding.root) {

        override fun bind(note: Note, isSelected: Boolean) {
            super.bind(note, isSelected)
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate
            binding.root.isSelected = isSelected

            setImage(note)
            setBackgroundColor(note, context)
        }
    }

    class NoteViewHolderStaggeredGrid(
        private val binding: ItemNoteStaggeredLayoutBinding,
        private val context: Context,
    ) : ViewHolder(binding.root) {

        override fun bind(note: Note, isSelected: Boolean) {
            super.bind(note, isSelected)
            binding.itemNoteTitle.text = note.title
            binding.itemNoteDescription.text = note.description
            binding.itemNoteModifiedDate.text = note.modifiedDate
            binding.root.isSelected = isSelected

            setImage(note)
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
        setupLongClickAction(holder)
        setupClickAction(holder, note)
    }

    private fun setupLongClickAction(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener {
            if (!isSelectedMode) {
                if (itemStateArray.contains(holder.adapterPosition)) {
                    itemStateArray.remove(holder.adapterPosition, false)
                } else {
                    itemStateArray.put(holder.adapterPosition, true)
                }
                changeViewState(it)

                onItemClickToSelectListener(null, itemStateArray, null, null, null, null)
            }

            isSelectedMode = true
            true
        }
    }

    private fun setupClickAction(
        holder: ViewHolder,
        note: Note?
    ) {
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

                onItemClickToSelectListener(null, itemStateArray, null, null, null, null)
            } else {
                onItemClickToSelectListener(
                    note,
                    itemStateArray,
                    holder.title,
                    holder.description,
                    holder.container,
                    holder.image
                )
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
        onItemClickToSelectListener(null, itemStateArray, null, null, null, null)
    }

    fun returnSelectedItems(id: Int?) {
        val map = mutableMapOf<Int, Note>()
        itemStateArray.forEach { key, _ ->
            map[key] = currentList[key]
        }

        selectedItemsActionListener(map, id)
        itemStateArray.clear()
        onItemClickToSelectListener(null, itemStateArray, null, null, null, null)
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

