package com.codepad.foodai.ui.core

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<TDataBinding: ViewDataBinding> : AppCompatActivity() {

    @LayoutRes
    abstract fun getLayoutId(): Int
    abstract fun onReadyView()

    open lateinit var binding: TDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
        onReadyView()
    }
}