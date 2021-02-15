package com.akash.dynamicadapter.interfaces

interface AdapterSelectionListener<T> {
    fun adapterSelectionChanged(objectList: ArrayList<T>, isSelected: Boolean)
}