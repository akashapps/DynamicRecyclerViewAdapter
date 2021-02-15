package com.akash.dynamicadapter.interfaces

interface OnRowLongClickAction <T: RootObject> {
    fun rowLongClickAction(item: T, position: Int)
}