package com.akash.dynamicadapter.interfaces

import com.akash.dynamicadapter.BaseViewHolder

interface OnLongClickListener {
    fun onLongClick(viewHolder: BaseViewHolder, position: Int)
}