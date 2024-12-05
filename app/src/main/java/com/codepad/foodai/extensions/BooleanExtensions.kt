@file:JvmName("BooleanExtensions")
package com.codepad.foodai.extensions

fun Boolean?.orFalse() = this ?: false

fun Boolean?.orTrue() = this ?: true
