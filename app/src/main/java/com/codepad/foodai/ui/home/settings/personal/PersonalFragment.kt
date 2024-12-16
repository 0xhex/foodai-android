package com.codepad.foodai.ui.home.settings.personal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentPersonalBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class PersonalFragment : BaseFragment<FragmentPersonalBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun onReadyView() {
        setupUI()
        viewModel.fetchUserData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.itemCurrentWeight.setOnClickListener {
            // Navigate to EditHeightWeightActivity
        }

        binding.itemHeight.setOnClickListener {
            // Navigate to EditHeightWeightActivity
        }

        binding.itemDateOfBirth.setOnClickListener {
            // Navigate to EditDateOfBirthActivity
        }

        binding.itemGender.setOnClickListener {
            // Navigate to EditGenderActivity
        }

        viewModel.userDataResponse.observe(viewLifecycleOwner) { userData ->
            userData?.let {
                binding.txtCurrentWeightValue.text = "${it.weight?.toInt()} kg"
                binding.txtHeightValue.text = "${it.height?.toInt()} cm"
                it.dateOfBirth?.let { dateOfBirth ->
                    try {
                        val parsedDate = isoDateFormat.parse(dateOfBirth)
                        binding.txtDateOfBirthValue.text = dateFormatter.format(parsedDate)
                    } catch (e: Exception) {
                        binding.txtDateOfBirthValue.text = "Invalid Date"
                    }
                }
                binding.txtGenderValue.text = it.gender?.capitalize() ?: "Unknown"
            }
        }
    }


    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
}