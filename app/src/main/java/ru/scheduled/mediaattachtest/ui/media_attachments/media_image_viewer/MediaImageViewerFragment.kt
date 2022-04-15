package ru.scheduled.mediaattachtest.ui.media_attachments.media_image_viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_media_image_viewer.*
import org.koin.android.ext.android.inject
import ru.scheduled.mediaattachtest.R
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes
import ru.scheduled.mediaattachtest.ui.base.BaseFragment
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.CURRENT_SHARD_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.INDEX_OF_IMAGE_CLICKED
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.MEDIA_TYPE
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.MediaNotesStates


class MediaImageViewerFragment : BaseFragment() {

    override val layoutResId: Int
        get() = R.layout.fragment_media_image_viewer

    private val viewModel by inject<MediaImageViewerViewModel>()

    private lateinit var mediaType: String
    private lateinit var shardId: String
    private lateinit var listOfNotes: List<DbMediaNotes>

    private var currentIndex = 0





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaType = it.getString(MEDIA_TYPE) ?: ""
            currentIndex = it.getInt(INDEX_OF_IMAGE_CLICKED) as? Int ?: 0
            shardId = it.getString(CURRENT_SHARD_ID) ?: ""
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaViewer.apply {
            setOnTryToLeaveCallback {
                findNavController().popBackStack()
            }
        }

        viewModel.state.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is MediaNotesStates.ErrorState -> {

                }
                MediaNotesStates.MediaNoteRemovedState -> {
                }

            }

        })


        viewModel.getMediaUrisByType(shardId, mediaType)?.observe(this, Observer {
            listOfNotes = it
            mediaViewer.setImageUris(listOfNotes.map { it.value },currentIndex)
        })

    }






}

