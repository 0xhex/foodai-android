package com.codepad.foodai.ui.home.settings.personal

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentPersonalBinding
import com.codepad.foodai.domain.models.user.User
import com.codepad.foodai.helpers.UserSession
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.HomeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.random.Random

@AndroidEntryPoint
class PersonalFragment : BaseFragment<FragmentPersonalBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    override val hideBottomNavBar: Boolean = true
    private val emojis = listOf("🙂", "😎", "🤩", "😇", "😃", "🤖", "🐱‍👤")
    private var randomEmoji = "🙂"

    override fun getLayoutId(): Int {
        return R.layout.fragment_personal
    }

    override fun onReadyView() {
        randomEmoji = emojis[Random.nextInt(emojis.size)]
        setupUI()
        viewModel.fetchUserData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup profile picture and edit button
        binding.profilePictureContainer.setOnClickListener {
            // TODO: Implement image picker functionality
            Snackbar.make(binding.root, getString(R.string.feature_coming_soon), Snackbar.LENGTH_SHORT).show()
        }

        // Setup name editing
        binding.nameContainer.setOnClickListener {
            showEditNameDialog()
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
                updateUI(it)
            }
        }
    }

    private fun updateUI(userData: User) {
        // Update profile picture
        if (!userData.profilePicUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(userData.profilePicUrl)
                .circleCrop()
                .into(binding.imgProfile)
            binding.txtProfileEmoji.visibility = View.GONE
        } else {
            binding.imgProfile.setImageResource(android.R.color.transparent)
            binding.txtProfileEmoji.visibility = View.VISIBLE
            binding.txtProfileEmoji.text = randomEmoji
        }

        // Update name
        binding.txtName.text = userData.name ?: getString(R.string.enter_your_name)
        binding.txtName.setTextColor(if (userData.name.isNullOrEmpty()) 
            resources.getColor(R.color.gray, null) else resources.getColor(R.color.white, null))

        // Update other fields
        binding.txtCurrentWeightValue.text = formatWeight(userData)
        binding.txtHeightValue.text = formatHeight(userData)
        
        userData.dateOfBirth?.let { dateOfBirth ->
            try {
                val parsedDate = isoDateFormat.parse(dateOfBirth)
                binding.txtDateOfBirthValue.text = dateFormatter.format(parsedDate)
            } catch (e: Exception) {
                binding.txtDateOfBirthValue.text = getString(R.string.unknown)
            }
        }
        
        binding.txtGenderValue.text = userData.gender?.capitalize() ?: getString(R.string.unknown)
    }

    private fun showEditNameDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null)
        val editText = dialogView.findViewById<EditText>(R.id.edit_name)
        editText.setText(viewModel.userDataResponse.value?.name ?: "")
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val newName = editText.text.toString().trim()
            if (newName.isNotEmpty()) {
                viewModel.updateUserField("name", newName)
            }
            dialog.dismiss()
        }
        
        dialog.show()
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