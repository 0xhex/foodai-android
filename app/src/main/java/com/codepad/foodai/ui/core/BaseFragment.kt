package com.codepad.foodai.ui.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.ui.home.analytics.AnalyticsTabFragment
import com.codepad.foodai.ui.home.home.HomeTabFragment
import com.codepad.foodai.ui.home.home.pager.goals.GoalViewFragment
import com.codepad.foodai.ui.home.home.pager.health.GoogleHealthFragment
import com.codepad.foodai.ui.home.home.pager.recipe.FoodRecipesFragment
import com.codepad.foodai.ui.home.settings.SettingsTabFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseFragment<TBinding : ViewDataBinding> : Fragment() {

    open lateinit var binding: TBinding
    open val hideBottomNavBar = false

    @LayoutRes
    protected abstract fun getLayoutId(): Int
    protected abstract fun onReadyView()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onReadyView()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    open fun navigate(activity: FragmentActivity, view: Int, action: Int) {
        view.let {
            Navigation.findNavController(activity, it).navigate(action)
        }
    }

    open fun navigate(activity: FragmentActivity, view: Int, direction: NavDirections) {
        view.let {
            Navigation.findNavController(activity, it).navigate(direction)
        }
    }

    open fun navigate(direction: NavDirections) {
        view?.let { _view -> Navigation.findNavController(_view).navigate(direction) }
    }

    open fun navigate(action: Int) {
        view?.let { _view ->
            Navigation.findNavController(_view).navigate(action)
        }
    }

    open fun navigate(action: Int, args: Bundle? = null) {
        view?.let { _view ->
            Navigation.findNavController(_view).navigate(action, args)
        }
    }

    override fun onStop() {
        super.onStop()
        showHideBottomNavigationView()
    }

    override fun onResume() {
        super.onResume()
        showHideBottomNavigationView()
        setIconsVisibility(this::class == AnalyticsTabFragment::class || this::class == HomeTabFragment::class || this::class == SettingsTabFragment::class || this::class == GoalViewFragment::class || this::class == GoogleHealthFragment::class || FoodRecipesFragment::class == this::class)
    }

    fun showHideBottomNavigationView(enable: Boolean? = null) {
        val bottomNavView =
            (activity as? BaseActivity<*>)?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
                ?: return
        val shouldEnable = enable ?: !hideBottomNavBar
        if (shouldEnable == bottomNavView.isVisible) return
        if (shouldEnable) {
            bottomNavView.visibility = View.VISIBLE
        } else {
            bottomNavView.visibility = View.GONE
        }
    }

    fun setIconsVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        activity?.findViewById<View>(R.id.img_circle)?.visibility = visibility
        activity?.findViewById<View>(R.id.img_plus)?.visibility = visibility
    }
}