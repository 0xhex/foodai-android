package com.codepad.foodai.ui.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment<DB : ViewDataBinding> : DialogFragment() {
    abstract val layoutResourcesId: Int
    private var _binding: DB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
}