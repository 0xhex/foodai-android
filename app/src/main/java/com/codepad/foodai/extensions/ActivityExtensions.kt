package com.codepad.foodai.extensions

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.codepad.foodai.R

fun Activity.showSuccessDialog(title: String, message: String, callback: DialogInterface.OnClickListener) {
    AlertDialog
            .Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(resources.getString(R.string.okey), callback)
            .show()
}


fun Activity.showErrorDialog(title: String, message: String, callback: DialogInterface.OnClickListener) {
    AlertDialog
            .Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(resources.getString(R.string.okey), callback)
            .show()
}