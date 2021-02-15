package com.akash.dynamicadapter.interfaces

import com.akash.dynamicadapter.BaseViewHolder

interface OnClickListener {
    fun onClick(viewHolder: BaseViewHolder, position: Int)
}