package com.codepad.foodai.ui.home.home.fooddetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.codepad.foodai.R
import com.codepad.foodai.databinding.BottomSheetMoreOptionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MoreOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMoreOptionsBinding? = null
    private val binding get() = _binding!!

    private var isShared: Boolean = false
    private var healthScore: Double = 0.0
    private var listener: OptionsListener? = null

    interface OptionsListener {
        fun onShareSelected()
        fun onShareCommunitySelected()
        fun onDeleteSelected()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetMoreOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Set up community share option visibility based on whether it's already shared
        binding.btnShareCommunity.visibility = if (isShared) View.GONE else View.VISIBLE
    }

    private fun setupClickListeners() {
        binding.btnShare.setOnClickListener {
            listener?.onShareSelected()
            dismiss()
        }

        binding.btnShareCommunity.setOnClickListener {
            listener?.onShareCommunitySelected()
            dismiss()
        }

        binding.btnDelete.setOnClickListener {
            listener?.onDeleteSelected()
            dismiss()
        }
    }

    private fun getHealthScoreColor(score: Double): Int {
        return when {
            score > 7.0 -> ContextCompat.getColor(requireContext(), R.color.green)
            score > 4.0 -> ContextCompat.getColor(requireContext(), R.color.yellow)
            else -> ContextCompat.getColor(requireContext(), R.color.red)
        }
    }

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (parentFragment is OptionsListener) {
            listener = parentFragment as OptionsListener
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(isShared: Boolean, healthScore: Double): MoreOptionsBottomSheet {
            val fragment = MoreOptionsBottomSheet()
            fragment.isShared = isShared
            fragment.healthScore = healthScore
            return fragment
        }
    }
} 