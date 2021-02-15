package com.akash.dynamicadapter.interfaces

interface OnSelectionListener<T> {
    fun onSelectionChange(list: List<T>, isSelected: Boolean)
}