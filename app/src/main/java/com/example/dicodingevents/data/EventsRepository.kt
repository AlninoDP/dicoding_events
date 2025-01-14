package com.example.dicodingevents.data


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.dicodingevents.data.local.entity.EventEntity
import com.example.dicodingevents.data.local.room.EventsDao
import com.example.dicodingevents.data.remote.retrofit.ApiService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsRepository private constructor(
    private val apiService: ApiService,
    private val eventsDao: EventsDao,
) {
    companion object {
        @Volatile
        private var instance: EventsRepository? = null

        fun getInstance(
            apiService: ApiService,
            eventsDao: EventsDao,
        ): EventsRepository = instance ?: synchronized(this) {
            instance ?: EventsRepository(apiService, eventsDao)
        }.also {
            instance = it
        }
    }

    fun getAllEvents(): LiveData<Result<List<EventEntity>>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getEvents()
            val events = response.listEvents
            val eventList = events.map { event ->
                val isFinished = isEventFinished(event.beginTime)
                val isBookmarked = eventsDao.isEventBookmarked(event.name!!)
                EventEntity(
                    eventId = event.id,
                    name = event.name,
                    summary = event.summary,
                    description = event.description,
                    imageLogo = event.imageLogo,
                    mediaCover = event.mediaCover,
                    ownerName = event.ownerName,
                    cityName = event.cityName,
                    quota = event.quota,
                    registrants = event.registrants,
                    beginTime = event.beginTime,
                    endTime = event.endTime,
                    link = event.link,
                    isFinished = isFinished,
                    isBookmarked = isBookmarked
                )
            }
            eventsDao.deleteAll()
            eventsDao.insertEvents(eventList)
        } catch (e: Exception) {
            Log.d("EventsRepository", "getAllEvents: ${e.message.toString()}")
            emit(Result.Error(e.message.toString()))
        }

        val localData: LiveData<Result<List<EventEntity>>> = eventsDao.getAllEvents().map {
            Result.Success()
        }
        emitSource(localData)
    }

    fun getBookmarkedEvent() :LiveData<List<EventEntity>>{
        return eventsDao.getBookmarkedEvents()
    }

    suspend fun setEventsBookmark(event: EventEntity, bookmarkState: Boolean) {
        event.isBookmarked = bookmarkState
        eventsDao.updateEvents(event)
    }

    fun getEventDetail(eventId: Int):  LiveData<EventEntity> {
        return eventsDao.getEventDetails(eventId)
    }

    fun getFinishedEvents(limit: Int? = 40): LiveData<List<EventEntity>> {
        return eventsDao.getFinishedEvents(limit = limit)
    }

    fun getUpcomingEvents(): LiveData<List<EventEntity>> {
        return eventsDao.getUpcomingEvents()
    }

    fun searchEvents(name:String?, isFinished: Int? = 0): LiveData<List<EventEntity>> {
        return eventsDao.searchEvent(name, isFinished)
    }

    private fun isEventFinished(beginTime: String?): Boolean {

        if (beginTime.isNullOrEmpty()) {
            return false
        }
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val eventTime = dateTimeFormat.parse(beginTime)
            val currentDateTime = Date()

            currentDateTime.after(eventTime)
        } catch (e: Exception) {
            false
        }


    }
}