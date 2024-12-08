package com.codepad.foodai.ui.onboarding.pager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codepad.foodai.databinding.FragmentOnboardingPageBinding

class OnboardingPageFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"

        fun newInstance(titleRes: Int, descRes: Int): OnboardingPageFragment {
            val fragment = OnboardingPageFragment()
            val args = Bundle()
            args.putInt(ARG_TITLE, titleRes)
            args.putInt(ARG_DESC, descRes)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentOnboardingPageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentOnboardingPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleRes = arguments?.getInt(ARG_TITLE) ?: 0
        val descRes = arguments?.getInt(ARG_DESC) ?: 0

        binding.title.setText(titleRes)
        binding.description.setText(descRes)
    }
}