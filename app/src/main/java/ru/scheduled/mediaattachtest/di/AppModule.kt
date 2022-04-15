package ru.scheduled.mediaattachtest.di


import androidx.room.Room
import org.koin.dsl.module
import ru.scheduled.mediaattachtest.db.media_uris.MediaNotesRepository
import ru.scheduled.mediaattachtest.db.MediaDatabase

val appModule = module {

    single<MediaDatabase> {
        Room.databaseBuilder(get(), MediaDatabase::class.java, "LFS.db")
        .build()
    }

    single<MediaNotesRepository> { MediaNotesRepository(get()) }

}

