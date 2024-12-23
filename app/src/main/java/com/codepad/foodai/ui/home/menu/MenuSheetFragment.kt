package com.codepad.foodai.ui.home.menu

import android.os.Bundle
import android.view.View
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentMenuBottomSheetBinding
import com.codepad.foodai.ui.core.BaseBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuSheetFragment : BaseBottomSheetFragment<FragmentMenuBottomSheetBinding>() {
    override val layoutResourcesId: Int = R.layout.fragment_menu_bottom_sheet

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.parent as View
        setRoundedCornerBottomSheetUI(bottomSheet)
        setStateAsExpanded(bottomSheet)
    }


    override fun listenClick() {
        super.listenClick()
        binding.apply {
            btnScanFood.setOnClickListener {

            }

            btnLogExercise.setOnClickListener {

            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}