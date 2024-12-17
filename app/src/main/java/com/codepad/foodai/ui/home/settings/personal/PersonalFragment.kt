package com.codepad.foodai.ui.home.settings.personal

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentPersonalBinding
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class PersonalFragment : BaseFragment<FragmentPersonalBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    override val hideBottomNavBar: Boolean = true

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
            navigateToHeightWeightFragment("weight")
        }

        binding.itemHeight.setOnClickListener {
            navigateToHeightWeightFragment("height")
        }

        binding.itemDateOfBirth.setOnClickListener {
            findNavController().navigate(
                PersonalFragmentDirections.actionPersonalFragmentToBirthFragment(
                    true
                )
            )
        }

        binding.itemGender.setOnClickListener {
            findNavController().navigate(
                PersonalFragmentDirections.actionPersonalFragmentToGenderFragment(
                    true
                )
            )
        }

        viewModel.userDataResponse.observe(viewLifecycleOwner) { userData ->
            userData?.let {
                binding.txtCurrentWeightValue.text = formatWeight(it)
                binding.txtHeightValue.text = formatHeight(it)
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

    private fun formatHeight(userData: User): String {
        return if (userData.isMetric == true) {
            "${userData.height?.toInt()} cm"
        } else {
            val totalInches = (userData.height ?: 0.0) / 2.54
            val feet = (totalInches / 12).toInt()
            val inches = (totalInches % 12).toInt()
            "$feet ft $inches in"
        }
    }

    private fun formatWeight(userData: User): String {
        return if (userData.isMetric == true) {
            "${userData.weight?.toInt()} kg"
        } else {
            "${userData.weight?.toInt()} lb"
        }
    }

    private fun navigateToHeightWeightFragment(type: String) {
        val action = PersonalFragmentDirections.actionPersonalFragmentToHeightWeightFragment(type)
        findNavController().navigate(action)
    }

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val isoDateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
}