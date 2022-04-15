package ru.scheduled.mediaattachtest.db

import androidx.room.*
import ru.scheduled.mediaattachtest.db.media_uris.AmplitudesConverter
import ru.scheduled.mediaattachtest.db.media_uris.MediaUrisDao
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes


@Database(
        entities = [
            DbMediaNotes::class
        ],
        version = 1,
        exportSchema = true
)
@TypeConverters(
    AmplitudesConverter::class
)
abstract class MediaDatabase : RoomDatabase() {
    abstract fun mediaUrisDao(): MediaUrisDao
}