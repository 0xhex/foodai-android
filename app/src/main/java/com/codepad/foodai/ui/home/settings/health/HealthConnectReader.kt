package com.codepad.foodai.ui.home.settings.health

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.codepad.foodai.extensions.getPriorDaysWithTimeMillisPairs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class HealthConnectReader @Inject constructor(
    private val healthConnectClient: HealthConnectClient,
) {

    suspend fun performHealthAction(onHealthConnectDataRead: (Pair<MutableList<Int>, MutableList<Int>>) -> Unit) {
        val stepsList = readSteps()

        withContext(Dispatchers.Default) {
            val processedStepsList = separateTwoDaysData(
                stepsList = stepsList.first, previousDayTimePair = stepsList.second.first()
            )
            onHealthConnectDataRead(processedStepsList)
        }
    }

    private suspend fun readSteps(date: Date = Date()): Pair<List<StepsRecord>, List<Pair<Long, Long>>> {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val dayCount = 7

        val timePairs = date.getPriorDaysWithTimeMillisPairs(dayCount)

        val endTime: Long = timePairs.last().second
        val startTime: Long = timePairs.first().first
        val timePairList: List<Pair<Long, Long>> = timePairs

        val stepsDeferred = CompletableDeferred<Pair<List<StepsRecord>, List<Pair<Long, Long>>>>()
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class, timeRangeFilter = TimeRangeFilter.between(
                        Instant.ofEpochMilli(startTime), Instant.ofEpochMilli(endTime)
                    )
                )
            )
            response.records?.let { stepsDeferred.complete(Pair(it, timePairList)) }
        } catch (e: Exception) {
            // Handle exception
        }

        return stepsDeferred.await()
    }

    private fun separateTwoDaysData(
        stepsList: List<StepsRecord>? = null,
        previousDayTimePair: Pair<Long, Long>,
    ): Pair<MutableList<Int>, MutableList<Int>> {
        val currentDayData = mutableListOf<Int>()
        val previousDayData = mutableListOf<Int>()

        stepsList?.forEach { item ->
            val dataOfPreviousDay =
                item.startTime.toEpochMilli() >= previousDayTimePair.first && item.endTime.toEpochMilli() <= previousDayTimePair.second
            if (dataOfPreviousDay) {
                val value = item.count.toInt()
                previousDayData.add(value)
            } else {
                val value = item.count.toInt()
                currentDayData.add(value)
            }
        }

        return Pair(currentDayData, previousDayData)
    }
}