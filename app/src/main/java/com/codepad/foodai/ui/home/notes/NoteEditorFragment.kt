package com.codepad.foodai.ui.home.notes

import MoodChipsAdapter
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentNoteEditorBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NoteEditorFragment : BaseFragment<FragmentNoteEditorBinding>() {
    private val args: NoteEditorFragmentArgs by navArgs()
    private val viewModel: HomePagerViewModel by activityViewModels()
    private lateinit var moodAdapter: MoodChipsAdapter
    private var selectedDate: Date = Date()
    private var selectedMoods = ""

    override fun getLayoutId(): Int = R.layout.fragment_note_editor
    override val hideBottomNavBar: Boolean = true

    override fun onReadyView() {
        selectedDate = args.selectedDate
        setupViews()
        setupMoodChips()
        loadExistingNote()
    }

    private fun setupViews() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            btnSave.setOnClickListener {
                saveNote()
            }

            editNote.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    updateCharCount()
                }
            })

            // Format date: "Today, February 15"
            val formatter = SimpleDateFormat("MMMM d", Locale.getDefault())
            txtDate.text = getString(R.string.today_date, formatter.format(selectedDate))
        }
    }

    private fun setupMoodChips() {
        moodAdapter = MoodChipsAdapter { moods ->
            selectedMoods = moods.joinToString(",")
        }

        binding.rvMoodChips.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = moodAdapter
        }
    }

    private fun loadExistingNote() {
        viewModel.getNote(selectedDate)?.let { note ->
            binding.editNote.setText(note.noteText)
            moodAdapter.setSelectedMoods(note.mood)
            updateCharCount()
        }
    }

    private fun updateCharCount() {
        val remaining = 512 - binding.editNote.text.length
        binding.txtCharCount.text = getString(R.string.characters_left, remaining)
    }

    private fun saveNote() {
        val noteText = binding.editNote.text.toString()
        viewModel.saveNote(selectedDate, noteText, selectedMoods)
        findNavController().navigateUp()
    }
} 