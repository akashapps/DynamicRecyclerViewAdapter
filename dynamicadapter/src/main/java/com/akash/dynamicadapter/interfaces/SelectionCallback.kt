package com.akash.dynamicadapter.interfaces

import com.akash.dynamicadapter.enums.SelectionState

interface SelectionCallback {
    fun setSelection(selectionState: SelectionState)
}