package com.akash.dynamicadapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.akash.dynamicadapter.enums.SelectionState
import com.akash.dynamicadapter.interfaces.OnClickListener
import com.akash.dynamicadapter.interfaces.OnLongClickListener
import com.akash.dynamicadapter.interfaces.SelectionCallback

class BaseViewHolder(var rootView: View) : RecyclerView.ViewHolder(rootView) {

    public var selectionState = SelectionState.Hidden
    public var selectionCallBack: SelectionCallback? = null

    public var onClickListener: OnClickListener? = null
        set(value) {
            field = value
            itemView.setOnClickListener {
                if (selectionState != SelectionState.Hidden){
                    toggleSelection()
                }

                field?.onClick(this, layoutPosition)
            }
        }

    public var onLongClickListener: OnLongClickListener? = null
        set(value) {
            field = value
            itemView.setOnLongClickListener {
                field?.onLongClick(this, layoutPosition)
                return@setOnLongClickListener true
            }
        }

    public fun setSelection(value: Boolean){

        selectionState = if (value){
            SelectionState.Selected
        }else{
            SelectionState.Unselected
        }

        selectionCallBack?.setSelection(selectionState)
    }

    public fun toggleSelection(){
        selectionState = if (selectionState == SelectionState.Selected){
            SelectionState.Unselected
        }else{
            SelectionState.Selected
        }

        selectionCallBack?.setSelection(selectionState)
    }
}