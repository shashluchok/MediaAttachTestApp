package ru.scheduled.mediaattachtest.ui.media_attachments.media_sketch

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.scheduled.mediaattachtest.db.media_uris.MediaNotesRepository
import ru.scheduled.mediaattachtest.ui.base.BaseViewModel
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.MediaNotesStates
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MediaSketchViewModel(private val mediaNotesInteractor: MediaNotesRepository,
    ): BaseViewModel<MediaNotesStates>() {

    private var mediaUris:LiveData<List<DbMediaNotes>>? = null

    fun updateMediaNote(dbMediaNote: DbMediaNotes) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaNotesInteractor.updateMediaNote(dbMediaNote)
            _state.postValue(MediaNotesStates.MediaNoteUpdatedState(dbMediaNote.id))
        }
    }

    fun saveDbMediaNotes(dbMediaNote: DbMediaNotes) {
        viewModelScope.launch(Dispatchers.IO) {
            mediaNotesInteractor.saveMediaNotes(dbMediaNote)
            _state.postValue(MediaNotesStates.MediaNoteSavedState)
        }
    }

    fun getDbMediaNoteById(dbMediaNoteId :String){
        viewModelScope.launch(Dispatchers.IO){
            try {
                val mediaNote = mediaNotesInteractor.getMediaNoteById(dbMediaNoteId)
                _state.postValue(MediaNotesStates.MediaNoteLoadedState(dbMediaNotes = mediaNote))
            }
            catch (e:java.lang.Exception){
                e.printStackTrace()
            }
        }
    }

    fun updateSketchMediaNote(sketchByteArray: ByteArray, shardId: String, dbMediaNote: DbMediaNotes) {
        viewModelScope.launch(Dispatchers.IO) {

            try {

                val oldFile = File(dbMediaNote.value)
                try {
                    if (oldFile.exists()) {
                        oldFile.delete()
                    }
                }
                catch (e:java.lang.Exception){
                    e.printStackTrace()
                }

                val mydir = appContext.getDir(MediaConstants.MEDIA_NOTES_INTERNAL_DIRECTORY, Context.MODE_PRIVATE)

                if (!mydir.exists()) {
                    mydir.mkdirs()
                }

                val fileName =
                        "$mydir/${System.currentTimeMillis()}"
                launch(Dispatchers.IO){
                    val f = File(fileName)
                    if (f.exists()) f.delete()
                    f.createNewFile()
                    val fos = FileOutputStream(f)
                    fos.write(sketchByteArray)
                    fos.flush()
                    fos.close()
                }.join()
                val newDbMediaNote = dbMediaNote
                newDbMediaNote.value = fileName
                updateMediaNote(newDbMediaNote)

            } catch (e: java.lang.Exception) {
                _state.postValue(MediaNotesStates.ErrorState("Ошибка при сохранении файла"))
            }

        }
    }

    fun saveMediaNoteBitmap(sketchByteArray: ByteArray, shardId: String, mediaType: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                val mydir = appContext.getDir(MediaConstants.MEDIA_NOTES_INTERNAL_DIRECTORY, Context.MODE_PRIVATE)

                if (!mydir.exists()) {
                    mydir.mkdirs()
                }

                val fileName =
                        "$mydir/${System.currentTimeMillis()}"
                launch(Dispatchers.IO){
                    val f = File(fileName)
                    if (f.exists()) f.delete()
                    f.createNewFile()
                    val fos = FileOutputStream(f)
                    fos.write(sketchByteArray)
                    fos.flush()
                    fos.close()
                }.join()
                val dbMediaUri = DbMediaNotes(
                    id = UUID.randomUUID().toString(),
                    shardId = shardId,
                    value = fileName,
                    mediaType = mediaType,
                    order = System.currentTimeMillis()
                )
                saveDbMediaNotes(dbMediaUri)

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                _state.postValue(MediaNotesStates.ErrorState("Ошибка при сохранении файла"))
            }

        }

    }


}