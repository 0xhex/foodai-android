package com.codepad.foodai.ui.home.settings.health

import com.codepad.foodai.R
import com.codepad.foodai.databinding.ActivityPermissionsRationaleBinding
import com.codepad.foodai.ui.core.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionsRationaleActivity : BaseActivity<ActivityPermissionsRationaleBinding>() {
    override fun getLayoutId(): Int = R.layout.activity_permissions_rationale
    override fun onReadyView() {
    }

}