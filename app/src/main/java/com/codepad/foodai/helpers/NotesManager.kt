package com.codepad.foodai.helpers

import android.content.SharedPreferences
import com.codepad.foodai.domain.models.note.DailyNote
import com.google.gson.Gson
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    private val gson = Gson()

    fun fetchNote(date: Date): DailyNote? {
        val key = DailyNote.dateKeyString(date)
        val noteJson = sharedPreferences.getString("note_$key", null)
        return noteJson?.let { gson.fromJson(it, DailyNote::class.java) }
    }

    fun saveNote(note: DailyNote) {
        val noteJson = gson.toJson(note)
        sharedPreferences.edit()
            .putString("note_${note.keyDate}", noteJson)
            .apply()
    }
} 