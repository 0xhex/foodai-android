package com.codepad.foodai.ui.home.home.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.note.DailyNote
import com.codepad.foodai.ui.home.home.adapter.MoodChipAdapter

class DailyNoteView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var noteContent: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var txtNoteContent: TextView
    private lateinit var btnStartJournaling: TextView
    private lateinit var rvMoodChips: RecyclerView
    private val moodAdapter = MoodChipAdapter { /* Read-only in this view */ }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_daily_note_content, this, true)
        initializeViews()
        setupMoodRecyclerView()
    }

    private fun initializeViews() {
        noteContent = findViewById(R.id.note_content)
        emptyState = findViewById(R.id.empty_state)
        txtNoteContent = findViewById(R.id.txt_note_content)
        btnStartJournaling = findViewById(R.id.btn_start_journaling)
        rvMoodChips = findViewById(R.id.rv_mood_chips)
    }

    private fun setupMoodRecyclerView() {
        rvMoodChips.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = moodAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    outRect.right = resources.getDimensionPixelSize(R.dimen.dimen_8dp)
                    outRect.left = if (parent.getChildAdapterPosition(view) == 0) {
                        resources.getDimensionPixelSize(R.dimen.dimen_8dp)
                    } else {
                        0
                    }
                }
            })
        }
    }

    fun updateNote(note: DailyNote?) {
        post {  // Ensure UI updates happen on main thread
            if (note != null && note.noteText.isNotEmpty()) {
                emptyState.visibility = View.GONE
                noteContent.visibility = View.VISIBLE
                txtNoteContent.text = note.noteText
                moodAdapter.setSelectedMoods(note.mood)
            } else {
                noteContent.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
                moodAdapter.setSelectedMoods("")
            }
        }
    }

    fun setOnStartJournalingClickListener(listener: () -> Unit) {
        btnStartJournaling.setOnClickListener { listener() }
    }

    fun setOnNoteClickListener(listener: () -> Unit) {
        noteContent.setOnClickListener { listener() }
    }
} 