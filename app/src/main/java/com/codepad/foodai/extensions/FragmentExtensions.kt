package com.codepad.foodai.extensions

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.codepad.foodai.R

fun Fragment.showSuccessDialog(
    title: String,
    message: String,
    button: Int? = null,
    callback: DialogInterface.OnClickListener,
) {
    AlertDialog.Builder(requireContext()).setTitle(title).setMessage(message)
        .setNeutralButton(resources.getString(button ?: R.string.okey), callback).show()
}


fun Fragment.showErrorDialog(
    title: String, message: String, callback: DialogInterface.OnClickListener,
) {
    AlertDialog.Builder(requireContext()).setTitle(title).setMessage(message)
        .setNeutralButton(resources.getString(R.string.okey), callback).show()
}