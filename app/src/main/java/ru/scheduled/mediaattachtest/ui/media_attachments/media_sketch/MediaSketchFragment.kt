package ru.leadfrog.features.detail.media_attachments.media_sketch

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.florent37.kotlin.pleaseanimate.please
import kotlinx.android.synthetic.main.fragment_media_sketch.*
import kotlinx.android.synthetic.main.layout_toolbar_default.view.*
import org.koin.android.ext.android.inject
import ru.scheduled.mediaattachtest.MainActivity
import ru.scheduled.mediaattachtest.R
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes
import ru.scheduled.mediaattachtest.isVisible
import ru.scheduled.mediaattachtest.ui.base.BaseFragment
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.CURRENT_SHARD_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.EXISTING_DB_MEDIA_NOTE_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.MediaNotesStates
import ru.scheduled.mediaattachtest.ui.media_attachments.media_sketch.MediaSketchViewModel


interface IOnBackPressed {
    fun onBackPressed(): Boolean
}

class MediaSketchFragment : BaseFragment(),IOnBackPressed {
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
            start_drawing_tv.isVisible = false
            currentDbMediaNoteId?.let {
            viewModel.getDbMediaNoteById(it)
            }
            toolbar_default.apply {
                toolbar_action_tv.setOnClickListener {
                    media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                            sketchToEdit?.let{ mediaNote ->
                                viewModel.updateSketchMediaNote(bitmap,shardId, mediaNote)
                            }
                        }
                }
            }
        }
        else {
            toolbar_default.apply {
                toolbar_action_tv.setTextColor(resources.getColor(R.color.color_primary_light))
            }
            media_sketch_drawing_view.setOnFirstTouchCallback {

                please(300) {
                    animate(start_drawing_tv){
                        invisible()
                    }
                }.start()
                toolbar_default.apply {
                    toolbar_action_tv.setTextColor(resources.getColor(R.color.colorPrimary))
                    toolbar_action_tv.setOnClickListener {
                        media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                            if(sketchToEdit==null) {
                                viewModel.saveMediaNoteBitmap( bitmap,shardId, "sketch")
                            }
                        }
                    }
                }
            }
        }
        toolbar_default.apply {
            toolbar_back_fl.setOnClickListener {
                if(media_sketch_drawing_view.wasAnythingDrawn()){
                        if(currentDbMediaNoteId!=null){
                            (requireActivity() as MainActivity).showPopupVerticalOptions(
                                topHeaderMessage = "Сохранить изменения?",
                                topActionText = "Отмена",
                                middleActionText = "Сохранить",
                                middleActionCallback = {
                                    if(sketchToEdit==null) {
                                        media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                            if(sketchToEdit==null) {
                                                viewModel.saveMediaNoteBitmap( bitmap,shardId, "sketch")
                                            }
                                        }
                                    }
                                    else {
                                        media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                            sketchToEdit?.let{ mediaNote ->
                                                viewModel.updateSketchMediaNote(bitmap,shardId, mediaNote)
                                            }
                                        }
                                    }

                                },
                                topActionCallback = {

                                }
                            )
                        }
                    else {
                            (requireActivity() as MainActivity).showPopupVerticalOptions(
                                topHeaderMessage = "Ваш скетч не сохранен",
                                secondHeaderMessage = "Нажмите кнопку Сохранить, чтобы применить изменения",
                                topActionText = "Удалить",
                                middleActionText = "Отмена",
                                bottomActionText = "Сохранить",
                                bottomActionCallback = {
                                    if(sketchToEdit==null) {
                                        media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                            if(sketchToEdit==null) {
                                                viewModel.saveMediaNoteBitmap( bitmap,shardId, "sketch")
                                            }
                                        }
                                    }
                                    else {
                                        media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                            sketchToEdit?.let{ mediaNote ->
                                                viewModel.updateSketchMediaNote(bitmap,shardId, mediaNote)
                                            }
                                        }
                                    }

                                },
                                topActionCallback = {
                                    findNavController().popBackStack()
                                }
                            )
                        }

                }
                else {
                    findNavController().popBackStack()
                }
            }
            toolbar_title.text = getString(R.string.sketch)
            toolbar_action_tv.text = getString(R.string.ready)
            toolbar_action_tv.visibility = View.VISIBLE

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

    }

    override fun onBackPressed(): Boolean {
        if(media_sketch_drawing_view.wasAnythingDrawn()){
            if(currentDbMediaNoteId!=null){
                (requireActivity() as MainActivity).showPopupVerticalOptions(
                    topHeaderMessage = "Сохранить изменения?",
                    topActionText = "Отмена",
                    middleActionText = "Сохранить",
                    middleActionCallback = {
                        if(sketchToEdit==null) {
                            media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                if(sketchToEdit==null) {
                                    viewModel.saveMediaNoteBitmap( bitmap,shardId, "sketch")
                                }
                            }
                        }
                        else {
                            media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                sketchToEdit?.let{ mediaNote ->
                                    viewModel.updateSketchMediaNote(bitmap,shardId, mediaNote)
                                }
                            }
                        }

                    },
                    topActionCallback = {

                    }
                )
            }
            else {
                (requireActivity() as MainActivity).showPopupVerticalOptions(
                    topHeaderMessage = "Ваш скетч не сохранен",
                    secondHeaderMessage = "Нажмите кнопку Сохранить, чтобы применить изменения",
                    topActionText = "Удалить",
                    middleActionText = "Отмена",
                    bottomActionText = "Сохранить",
                    bottomActionCallback = {
                        if(sketchToEdit==null) {
                            media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                if(sketchToEdit==null) {
                                    viewModel.saveMediaNoteBitmap( bitmap,shardId, "sketch")
                                }
                            }
                        }
                        else {
                            media_sketch_drawing_view.getSketchByteArray()?.let{ bitmap->
                                sketchToEdit?.let{ mediaNote ->
                                    viewModel.updateSketchMediaNote(bitmap,shardId, mediaNote)
                                }
                            }
                        }

                    },
                    topActionCallback = {
                        findNavController().popBackStack()
                    }
                )
            }
           return false
        } else return true
    }


}