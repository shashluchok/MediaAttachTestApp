package ru.scheduled.mediaattachtest.ui.media_attachments.media_image_viewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.scheduled.mediaattachtest.db.media_uris.MediaNotesRepository
import ru.scheduled.mediaattachtest.ui.base.BaseViewModel
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.MediaNotesStates
import java.io.File

class MediaImageViewerViewModel(
    private val mediaNotesInteractor: MediaNotesRepository,
) : BaseViewModel<MediaNotesStates>() {

    private var typedMediaUris:LiveData<List<DbMediaNotes>>? = null

    fun deleteFile(path:String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                file.delete()
            }
            catch (e:Exception){
               e.printStackTrace()
            }
        }
    }

    fun getMediaUrisByType(shardId:String, mediaType:String):LiveData<List<DbMediaNotes>>? {
        if(typedMediaUris==null){
            typedMediaUris = mediaNotesInteractor.getMediaNotesByType(shardId, mediaType)
        }
        return typedMediaUris
    }



}
