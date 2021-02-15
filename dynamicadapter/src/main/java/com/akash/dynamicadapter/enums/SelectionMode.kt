package com.akash.dynamicadapter.enums

enum class SelectionMode(val value: Int) {
    None(0),
    Single(1),
    Multiple(2);

    fun toInt(): Int{
        return value
    }
}