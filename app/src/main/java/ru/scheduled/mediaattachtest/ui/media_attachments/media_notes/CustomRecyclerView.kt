package ru.scheduled.mediaattachtest.ui.media_attachments.media_notes

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.adapters.MediaNotesAdapter

class CustomRecyclerView : RecyclerView {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?) : super(context!!)

    enum class MediaItemTypes {
        TYPE_SKETCH,
        TYPE_VOICE,
        TYPE_PHOTO,
        TYPE_TEXT,
//        TYPE_VIDEO
    }

    init {
        this.layoutManager = CustomLinearLayoutManager(context).also {
            it.stackFromEnd = true
        }

    }

    fun releasePlayer() {
        if (this.adapter !is MediaNotesAdapter) {
            return
        }
        (this.adapter as MediaNotesAdapter).releasePlayer()
    }

    fun initRecycler(
        mediaPlayer: MediaPlayer,
        onItemsSelected: (List<MediaNote>) -> Unit

    ) {
        this.adapter = MediaNotesAdapter(
            onItemsSelected = onItemsSelected
        ).also {
            it.setMediaPlayer(mediaPlayer)
        }

    }

    fun getSelectedMediaNotes(): List<MediaNote>{
        return (this.adapter as MediaNotesAdapter).getSelectedItems()
    }

    fun setData(data: List<MediaNote>) {
        if (this.adapter !is MediaNotesAdapter) {
            return
        } else {
            (this.adapter as MediaNotesAdapter).setData(data)
        }
    }

    fun stopSelecting(){
        (this.adapter as MediaNotesAdapter).stopSelecting()
    }

    data class MediaNote(
        val id:String,
        val mediaType: MediaItemTypes,
        val value: String,
        val recognizedSpeechText: String,
        val voiceAmplitudesList: List<Int>,
        val imageNoteText: String,
        val timeStamp:Long,
        var isChosen:Boolean = false

    )
}