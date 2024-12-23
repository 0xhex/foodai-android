package com.codepad.foodai.ui.home.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentMenuBottomSheetBinding
import com.codepad.foodai.ui.core.BaseBottomSheetFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.codepad.foodai.ui.home.MenuOption
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuSheetFragment : BaseBottomSheetFragment<FragmentMenuBottomSheetBinding>() {
    override val layoutResourcesId: Int = R.layout.fragment_menu_bottom_sheet
    private val viewModel: HomeViewModel by activityViewModels()

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
                viewModel.setOptionSelected(MenuOption.SCAN_FOOD)
            }

            btnLogExercise.setOnClickListener {
                viewModel.setOptionSelected(MenuOption.LOG_FOOD)
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}