package com.codepad.foodai.ui.home.notes

import MoodChipsAdapter
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.codepad.foodai.R
import com.codepad.foodai.databinding.FragmentNoteEditorBinding
import com.codepad.foodai.databinding.ViewNoteEditorToolbarBinding
import com.codepad.foodai.ui.core.BaseFragment
import com.codepad.foodai.ui.home.home.pager.HomePagerViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView

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
            // Toolbar setup
            toolbar.btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
            toolbar.txtTitle.text = getString(R.string.notes)
            toolbar.btnSave.setOnClickListener {
                saveNote()
            }

            // Date format
            val formatter = SimpleDateFormat("MMMM d", Locale.getDefault())
            txtDate.text = getString(R.string.today_date, formatter.format(selectedDate))

            // Note editor
            editNote.apply {
                setHint(R.string.add_notes_here)
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        updateCharCount()
                    }
                })
            }
        }
    }

    private fun setupMoodChips() {
        moodAdapter = MoodChipsAdapter({ moods ->
            selectedMoods = moods.joinToString(",")
        })

        // Calculate span count dynamically
        val screenWidth = resources.displayMetrics.widthPixels
        val horizontalPadding = resources.getDimensionPixelSize(R.dimen.dimen_16dp) * 2
        val chipSpacing = resources.getDimensionPixelSize(R.dimen.dimen_4dp) * 2
        val availableWidth = screenWidth - horizontalPadding

        // Calculate minimum chip width based on longest text
        val paint = android.graphics.Paint().apply {
            textSize = resources.getDimensionPixelSize(R.dimen.dimen_14dp).toFloat() // Chip text size
        }
        
        // Get longest chip width
        val longestChipText = getString(R.string.mood_stomach_ache) // One of the longer mood texts
        val textWidth = paint.measureText(longestChipText)
        val chipPadding = resources.getDimensionPixelSize(R.dimen.dimen_16dp) * 2 // Horizontal padding
        val minChipWidth = (textWidth + chipPadding).toInt()

        // Calculate optimal span count
        val spanCount = (availableWidth / (minChipWidth + chipSpacing)).coerceIn(2, 3)

        binding.rvMoodChips.apply {
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = moodAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.left = resources.getDimensionPixelSize(R.dimen.dimen_4dp)
                    outRect.right = resources.getDimensionPixelSize(R.dimen.dimen_4dp)
                    outRect.top = resources.getDimensionPixelSize(R.dimen.dimen_4dp)
                    outRect.bottom = resources.getDimensionPixelSize(R.dimen.dimen_4dp)
                }
            })
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