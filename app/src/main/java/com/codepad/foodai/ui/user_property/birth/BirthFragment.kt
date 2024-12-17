package com.codepad.foodai.ui.user_property.birth

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentBirthBinding
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.user_property.UserPropertySharedViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class BirthFragment : BaseFragment<FragmentBirthBinding>() {

    private val sharedViewModel: UserPropertySharedViewModel by activityViewModels()

    override val hideBottomNavBar: Boolean = true
    override fun getLayoutId() = R.layout.fragment_birth

    override fun onReadyView() {
        binding.viewModel = sharedViewModel

        binding.etBirthDate.setOnClickListener {
            showDatePickerDialog()
        }

        val isEdit = arguments?.getBoolean("isEdit") ?: false
        setPreDefinedDate(isEdit)
    }

    private fun setPreDefinedDate(edit: Boolean) {
        if (edit) {
            binding.tvTitle.visibility = android.view.View.GONE
            binding.tvSubtitle.visibility = android.view.View.GONE
            binding.nextButton.visibility = android.view.View.VISIBLE
            binding.clTopHeader.visibility = android.view.View.VISIBLE
            binding.etBirthDate.setText(UserSession.user?.getParsedDate())

            binding.nextButton.setOnClickListener {
                sharedViewModel.updateUserBirthDate()
                lifecycleScope.launch {
                    delay(1000)
                    findNavController().popBackStack()
                }
            }

            binding.btnBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.YEAR, -120)
        val startDate = calendar.timeInMillis

        val defaultDate = UserSession.user?.dateAsObject?.time ?: Calendar.getInstance().apply {
            set(1994, Calendar.JANUARY, 2)
        }.timeInMillis

        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(startDate)
            .setEnd(today)
            .setValidator(DateValidatorPointForward.from(startDate))

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.birth_date)
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.MaterialCalendarTheme)
            .setSelection(defaultDate)
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