package com.codepad.foodai.ui.user_property.birth

import androidx.fragment.app.activityViewModels
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentBirthBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class BirthFragment : BaseFragment<FragmentBirthBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_birth

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        binding.etBirthDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -120)
        val startDate = calendar.timeInMillis

        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(startDate)
            .setEnd(today)
            .setValidator(DateValidatorPointForward.from(startDate))

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.birth_date)
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.MaterialCalendarTheme)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            sharedViewModel.setDateOfBirth(Date(selection))
            val selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selection)
            binding.etBirthDate.setText(selectedDate)
        }

        datePicker.show(parentFragmentManager, "MATERIAL_DATE_PICKER")
    }

    override fun onResume() {
        super.onResume()
        if (sharedViewModel.dateOfBirth.value != null) {
            val selectedDate = SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(sharedViewModel.dateOfBirth.value!!)
            binding.etBirthDate.setText(selectedDate)
        }
    }
}