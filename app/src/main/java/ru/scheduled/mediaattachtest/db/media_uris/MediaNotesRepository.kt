package ru.scheduled.mediaattachtest.db.media_uris

import androidx.lifecycle.LiveData
import ru.scheduled.mediaattachtest.db.MediaDatabase
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes

class MediaNotesRepository(
    private val storage: MediaDatabase
){

    fun saveMediaNotes(dbMediaNote: DbMediaNotes) {
        storage.mediaUrisDao().insertMediaNote(dbMediaNote)
    }

    fun getAllMediaNotesByShardId(shardId:String): LiveData<List<DbMediaNotes>> {
        return storage.mediaUrisDao().getAllMediaNotesByShardId(shardId)
    }

    fun getAllMediaNotes(): LiveData<List<DbMediaNotes>> {
        return storage.mediaUrisDao().getAllMediaNotes()
    }

    fun removeMediaNotes(dbMediaNotes: List<DbMediaNotes>) {
        storage.mediaUrisDao().deleteMediaNotes(dbMediaNotes)
    }

    fun removeAllMediaNotes() {
        storage.mediaUrisDao().deleteAllMediaNotes()
    }

    fun getMediaNotesByType(shardId:String, mediaType: String): LiveData<List<DbMediaNotes>> {
        return storage.mediaUrisDao().getMediaNotesByType(shardId,mediaType)
    }

    fun getMediaNoteById(id: String): DbMediaNotes {
        return storage.mediaUrisDao().getMediaNoteById(id = id)
    }

    fun updateMediaNote(dbMediaNote: DbMediaNotes){
        storage.mediaUrisDao().updateMediaNote(dbMediaNote)
    }

}