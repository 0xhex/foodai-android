package com.codepad.foodai.ui.home.menu

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.codepad.foodai.R
import com.codepad.foodai.databinding.DialogSelectCameraOptionBinding
import com.codepad.foodai.ui.core.BaseDialogFragment

class SelectCameraOptionDialog(
    private val onOptionSelected: (Int) -> Unit
) : BaseDialogFragment<DialogSelectCameraOptionBinding>() {

    override val layoutResourcesId = R.layout.dialog_select_camera_option
    override fun getTheme() = R.style.RoundedCornersDialog

    override fun onInitView() {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val window: Window? = dialog?.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        binding.btnCamera.setOnClickListener {
            onOptionSelected(0)
            dismiss()
        }

        binding.btnGallery.setOnClickListener {
            onOptionSelected(1)
            dismiss()
        }

        binding.txtCancel.setOnClickListener {
            dismiss()
        }
    }
}