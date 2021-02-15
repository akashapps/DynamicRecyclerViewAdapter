package com.akash.dynamicadapter.interfaces

interface OnRowClickAction<T : RootObject> {
    fun rowClickAction(item: T, position: Int)
}