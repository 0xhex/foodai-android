package com.codepad.foodai.ui.core

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetFragment<DB : ViewDataBinding> : BottomSheetDialogFragment() {
    abstract val layoutResourcesId: Int
    private var _binding: DB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(layoutInflater, layoutResourcesId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        onInitView()
        listenClick()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeViewModel()
    }

    open fun onInitView() {}

    open fun listenClick() {}

    open fun subscribeViewModel() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setStateAsExpanded(bottomSheet: View) {
        dialog?.setOnShowListener {
            BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                setPeekHeight(0)
            }
        }
    }

    fun setRoundedCornerBottomSheetUI(bottomSheet: View) {
        bottomSheet.apply {
            backgroundTintMode = PorterDuff.Mode.CLEAR
            backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun setFullHeight(bottomSheet: View) {
        dialog?.apply {
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.peekHeight = resources.displayMetrics.heightPixels
            behavior.skipCollapsed = true
            view?.requestLayout()
        }
    }
}