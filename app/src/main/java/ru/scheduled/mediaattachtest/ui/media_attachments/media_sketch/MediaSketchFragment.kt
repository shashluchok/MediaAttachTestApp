package ru.leadfrog.features.detail.media_attachments.media_sketch

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_media_sketch.*
import org.koin.android.ext.android.inject
import ru.scheduled.mediaattachtest.R
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes
import ru.scheduled.mediaattachtest.ui.base.BaseFragment
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.CURRENT_SHARD_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.EXISTING_DB_MEDIA_NOTE_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.MediaNotesStates
import ru.scheduled.mediaattachtest.ui.media_attachments.media_sketch.MediaSketchViewModel


class MediaSketchFragment : BaseFragment() {
    override val layoutResId: Int
        get() = R.layout.fragment_media_sketch

    private var isEraserEnabled = false

    private val viewModel by inject<MediaSketchViewModel>()

    private var sketchToEdit: DbMediaNotes? = null
    private  var currentDbMediaNoteId: String? = null

    private lateinit var shardId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{
            if(it.getString(CURRENT_SHARD_ID)!=null){
                shardId = it.getString(CURRENT_SHARD_ID)!!
            }
            it.getString(EXISTING_DB_MEDIA_NOTE_ID)?.let{ id->
                currentDbMediaNoteId = id
            }
        }
    }

    private fun onEditExistingSketch(sketchMediaNote:DbMediaNotes){
        sketchToEdit = sketchMediaNote.also { mediaNote->
            val drawable = BitmapDrawable.createFromPath(mediaNote.value)
            drawable?.let{
                media_sketch_drawing_view.setExistingSketch(it)
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        media_sketch_drawing_view.setEdittingToolbarVisibility(isVisible = true)

        if(currentDbMediaNoteId!=null){
            currentDbMediaNoteId?.let {
            viewModel.getDbMediaNoteById(it)
            }
        }


        viewModel.state.observe(viewLifecycleOwner, Observer {
            when(it){
                is MediaNotesStates.MediaNoteSavedState ->{
                    findNavController().popBackStack()
                }
                is MediaNotesStates.MediaNoteUpdatedState ->{
                    findNavController().popBackStack()
                }
                is MediaNotesStates.MediaNoteLoadedState -> {
                    onEditExistingSketch(it.dbMediaNotes)
                }

            }

        })

        media_sketch_toolbar_back.setOnClickListener{
            findNavController().popBackStack()
        }


        sketch_ready_tv.setOnClickListener {
            media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->

                if(sketchToEdit==null) {
                   viewModel.saveMediaNoteBitmap( bitmap,shardId, "sketch")
                }
                else{
                    sketchToEdit?.let{ mediaNote ->
                        viewModel.updateSketchMediaNote(bitmap,shardId, mediaNote)
                    }
                }
            }


        }


    }




}