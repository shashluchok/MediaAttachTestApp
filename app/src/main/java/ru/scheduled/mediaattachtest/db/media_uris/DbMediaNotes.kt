package ru.scheduled.mediaattachtest.db.media_uris

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "mediaNotes")
data class DbMediaNotes(
        @PrimaryKey
        val id: String,
        val shardId:String,
        var value:String,
        val mediaType:String,
        val order:Long,
        var recognizedSpeechText:String = "",
        var imageNoteText:String = "",
        @TypeConverters(AmplitudesConverter::class)
        val voiceAmplitudesList:List<Int>? = listOf(),
)