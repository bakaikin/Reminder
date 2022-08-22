package com.bakaikin.sergey.reminder.adapter

sealed class ListElementType{
    object TypeTask : ListElementType()
    object TypeSeparator : ListElementType()
}
