package com.codepad.foodai.ui.main

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.viewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.ActivityMainBinding
import com.codepad.foodai.ui.core.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel: MainViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onReadyView() {
    }

    override fun attachBaseContext(base: Context) {
        // TODO: super.attachBaseContext(LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}