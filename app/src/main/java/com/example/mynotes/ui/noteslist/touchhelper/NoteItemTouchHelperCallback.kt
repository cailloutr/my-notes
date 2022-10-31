package com.example.mynotes.ui.noteslist.touchhelper

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.ui.noteslist.NotesListAdapter
import com.example.mynotes.ui.viewModel.NotesListViewModel

class NoteItemTouchHelperCallback(
    private val adapter: NotesListAdapter,
    private val viewModel: NotesListViewModel,
) : ItemTouchHelper.Callback() {

    var dragFrom = -1
    var dragTo = -1

    var orderChanged: Boolean = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        val onMoveTouchHelper = (ItemTouchHelper.UP or ItemTouchHelper.DOWN
                or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                )

        return makeMovementFlags(0, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition

        if(dragFrom == -1) {
            dragFrom =  fromPosition
        }
        dragTo = toPosition

        orderChanged = true
        adapter.onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && orderChanged) {
            viewModel.updateListPositions(dragFrom.toLong(), dragTo.toLong())
            adapter.notifyDataSetChanged()
            orderChanged = false
        }
    }

    //    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
//        super.clearView(recyclerView, viewHolder)
//
//        if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
//            viewModel.updateListPositions(dragFrom.toLong(), dragTo.toLong())
//        }
//
//        dragFrom = -1
//        dragTo = -1
//    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("Not yet implemented")
    }

}
