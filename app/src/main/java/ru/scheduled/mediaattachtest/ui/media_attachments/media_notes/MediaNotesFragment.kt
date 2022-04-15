package ru.scheduled.mediaattachtest.ui.media_attachments.media_notes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.android.synthetic.main.fragment_media_notes.*
import kotlinx.android.synthetic.main.layout_toolbar_default.view.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import ru.leadfrog.common.ActivityRequestCodes.Companion.ACTIVITY_REQUEST_CODE_PICK_PHOTO
import ru.leadfrog.common.PermissionRequestCodes.Companion.PERMISSION_REQUEST_CODE_CAMERA
import ru.leadfrog.common.PermissionRequestCodes.Companion.PERMISSION_REQUEST_CODE_STORAGE
import ru.scheduled.mediaattachmentslibrary.ToolTip
import ru.scheduled.mediaattachtest.MainActivity
import ru.scheduled.mediaattachtest.R
import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes
import ru.scheduled.mediaattachtest.isVisible
import ru.scheduled.mediaattachtest.toPx
import ru.scheduled.mediaattachtest.ui.base.BaseFragment
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.CURRENT_SHARD_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.EXISTING_DB_MEDIA_NOTE_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.EXISTING_PHOTO_PATH
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.IS_NEED_TO_SAVE_TO_GALLERY
import java.util.*

const val SHARD_ID = "123"

class MediaNotesFragment : BaseFragment() {
    override val layoutResId: Int
        get() = R.layout.fragment_media_notes

    private var currentTextNoteToEdit: DbMediaNotes? = null
    private val viewModel by inject<MediaNotesViewModel>()
    private lateinit var shardId: String

    private var mediaPlayer: MediaPlayer? = null
    private var isListEmpty = false

    private var animationJob: Job? = null
    private var initNotificationHeight = 0
    private var initNotificationY = 0f

    private var initToolbarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.getString(CURRENT_SHARD_ID) != null) {
                shardId = it.getString(CURRENT_SHARD_ID)!!
            } else {
                findNavController().popBackStack()
            }
        }
        shardId = SHARD_ID
    }

    private fun cancelSelecting() {
        media_notes_recycler_view.stopSelecting()
        selection_toolbar_cl.isVisible = false
        toolbar_default.isVisible = true
        mediaToolbarView.hideMediaEditingToolbar()
    }

    private fun onTextMediaCopied(text: String) {
        animationJob?.cancel()
        animationJob = null
        blurView.setBlurEnabled(false)

        media_text_copied_notification_cl?.apply {
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .duration = 0
        }

        val vibe = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibe?.vibrate(VibrationEffect.createOneShot(100, 1))
        } else vibe?.vibrate(100);

        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", text)
        clipboard.setPrimaryClip(clip)

        animationJob = lifecycleScope.launch(Dispatchers.IO) {
            delay(100)
            withContext(Dispatchers.Main) {
                media_text_copied_notification_cl?.apply {
                    animate()
                        .alpha(1f)
                        .setDuration(200).interpolator = DecelerateInterpolator()
                }

            }
            delay(200)
            withContext(Dispatchers.Main) {
                blurView.setBlurEnabled(true)

            }

            delay(2700)
            withContext(Dispatchers.Main) {
                media_text_copied_notification_cl?.apply {
                    animate()
                        .alpha(0f)
                        .y(initNotificationY).duration = 200
                }
                blurView.setBlurEnabled(false)
            }
            delay(250)
            withContext(Dispatchers.Main) {
                media_text_copied_notification_cl?.apply {
                    visibility = View.GONE
                }
            }
        }
    }

    private fun dismissNotification() {
        animationJob?.cancel()
        animationJob = null
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                media_text_copied_notification_cl?.apply {
                    animate()
                        .alpha(0f)
                        .y(initNotificationY).duration = 200
                }
                blurView.setBlurEnabled(false)
            }
            delay(250)
            withContext(Dispatchers.Main) {
                media_text_copied_notification_cl?.apply {
                    visibility = View.GONE
                }
            }
        }

    }

    private fun changeRecyclerPadding(isGrowing: Boolean, newHeight:Int) {
        if (isGrowing) {
            lifecycleScope.launch(Dispatchers.IO) {
                if (media_notes_recycler_view.paddingBottom >= newHeight) return@launch
                for (i in media_notes_recycler_view.paddingBottom ..(newHeight).toInt()) {
                    delay(1)
                    withContext(Dispatchers.Main) {
                        media_notes_recycler_view.setPadding(0, 0, 0, i)
                    }
                }
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                for (i in (media_notes_recycler_view.paddingBottom).toInt() downTo newHeight) {
                    delay(1)
                    withContext(Dispatchers.Main) {
                        media_notes_recycler_view.setPadding(0, 0, 0, i)
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        media_text_copied_notification_cl.setOnTouchListener { view, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    dismissNotification()
                }
            }
            true
        }

        val decorView = media_notes_cl
        val rootView =
            (toolbar_container_cl as ViewGroup)
        val windowBackground = decorView.background
        val radius = 5f
        blurView.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(requireContext()))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)

        toolbar_default.apply {
            toolbar_back_fl.setOnClickListener {
                findNavController().popBackStack()
            }

            toolbar_title.text = "Ярослав Фрозар"
        }

        media_text_copied_notification_cl?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                media_text_copied_notification_cl?.viewTreeObserver?.removeOnGlobalLayoutListener(
                    this
                )
                initNotificationY = media_text_copied_notification_cl?.y ?: 0f
                initNotificationHeight = media_text_copied_notification_cl?.height ?: 0

            }
        })

        mediaPlayer = MediaPlayer().also {
            media_notes_recycler_view.initRecycler(
                mediaPlayer = it,
                onItemsSelected = {
                    hideKeyboard()
                    selection_toolbar_cl.isVisible = it.isNotEmpty()
                    val isCopyable =
                        it.size == 1 && it.first().mediaType == CustomRecyclerView.MediaItemTypes.TYPE_TEXT

                    val isEditable = it.size == 1 && (listOf<CustomRecyclerView.MediaItemTypes>(
                        CustomRecyclerView.MediaItemTypes.TYPE_TEXT,
                        CustomRecyclerView.MediaItemTypes.TYPE_PHOTO,
                        CustomRecyclerView.MediaItemTypes.TYPE_SKETCH
                    ).contains(it.first().mediaType))

                    toolbar_default.isVisible = !it.isNotEmpty()
                    if (it.isNotEmpty()) {
//                        changeRecyclerPadding(isGrowing = false)
                        mediaToolbarView.showMediaEditingToolbar(
                            isCopyable = isCopyable,
                            isEditable = isEditable
                        )
                        selected_items_count_tv.text = it.size.toString()
                    } else {
//                        changeRecyclerPadding(isGrowing = true)
                        mediaToolbarView.hideMediaEditingToolbar()
                    }
                }

            )
        }

        selection_toolbar_cancel_iv.setOnClickListener {
            cancelSelecting()
        }

        mediaToolbarView.apply {

            setOnNewToolbarHeightCallback {
                    changeRecyclerPadding (isGrowing = it>media_notes_recycler_view.paddingBottom, newHeight = it)
            }

            setOnMediaEditingCancelClickedCallback {
                cancelSelecting()
            }

            setOnMediaCopyClickedCallback {
                val singleMedia = media_notes_recycler_view.getSelectedMediaNotes().singleOrNull()
                singleMedia?.let {
                    when (it.mediaType) {

                        CustomRecyclerView.MediaItemTypes.TYPE_TEXT -> {
                            onTextMediaCopied(it.value)
                        }
                        else -> {

                        }
                    }
                    cancelSelecting()
                }
            }

            setOnMediaDeleteClickedCallback {
                val selectedItems = media_notes_recycler_view.getSelectedMediaNotes()
                if (selectedItems.size == 1) {
                    (requireActivity() as MainActivity).showPopupVerticalOptions(
                        topHeaderMessage = " Удаление заметки",
                        secondHeaderMessage = "Вы действительно хотите удалить заметку?",
                        topActionText = "Удалить",
                        middleActionText = "Отмена",
                        topActionCallback = {
                            selectedItems.firstOrNull()?.let {
                                viewModel.deleteMediaNotes(
                                    listOf(it.toDbMediaNote())
                                )
                                cancelSelecting()
                            }

                        }
                    )
                } else if (selectedItems.size > 1) {
                    (requireActivity() as MainActivity).showPopupVerticalOptions(
                        topHeaderMessage = "Удаление заметок",
                        secondHeaderMessage = "Вы действительно хотите удалить заметки?",
                        topActionText = "Удалить",
                        middleActionText = "Отмена",
                        topActionCallback = {
                            viewModel.deleteMediaNotes(
                                selectedItems.map { it.toDbMediaNote() }
                            )
                            cancelSelecting()
                        }
                    )
                }
            }

            setOnMediaEditClickedCallback {
                val selectedItems = media_notes_recycler_view.getSelectedMediaNotes()
                selectedItems.singleOrNull()?.let {
                    when (it.mediaType) {
                        CustomRecyclerView.MediaItemTypes.TYPE_SKETCH -> {
                            findNavController().navigate(
                                R.id.action_mediaNotesFragment_to_mediaSketchFragment, bundleOf(
                                    CURRENT_SHARD_ID to shardId,
                                    EXISTING_DB_MEDIA_NOTE_ID to it.id
                                )
                            )
                        }
                        CustomRecyclerView.MediaItemTypes.TYPE_PHOTO -> {
                            findNavController().navigate(
                                R.id.action_mediaNotesFragment_to_imageCropFragment, bundleOf(
                                    CURRENT_SHARD_ID to shardId,
                                    EXISTING_DB_MEDIA_NOTE_ID to it.id
                                )
                            )
                        }
                        CustomRecyclerView.MediaItemTypes.TYPE_TEXT -> {
                            cancelSelecting()
                            currentTextNoteToEdit = it.toDbMediaNote()
                            mediaToolbarView.setText(it.value)
//                            changeRecyclerPadding(isGrowing = true)

                        }
                    }
                }
            }

            setOnToolBarReadyCallback { toolbarHeight ->
                initToolbarHeight = toolbarHeight
                media_notes_recycler_view.setPadding(0, 0, 0, toolbarHeight)
                media_notes_recycler_view.clipToPadding = false
            }

            setOnSendTextCallback { text ->
                        if (!text.isNullOrEmpty()) {

                            if (currentTextNoteToEdit != null) {
                                currentTextNoteToEdit?.let {
                                    it.value = text
                                    viewModel.updateMediaNote(it)
                                }
                                currentTextNoteToEdit = null

                            } else {
                                val dbMediaNote = DbMediaNotes(
                                    id = UUID.randomUUID().toString(),
                                    shardId = shardId,
                                    value = text,
                                    mediaType = "text",
                                    order = System.currentTimeMillis()
                                )
                                viewModel.saveDbMediaNotes(dbMediaNote)
                            }
                            return@setOnSendTextCallback true

                        } else {
                            if (currentTextNoteToEdit != null) {
                                (requireActivity() as MainActivity).showPopupVerticalOptions(
                                    topHeaderMessage = "Удалить эту заметку?",
                                    topActionText = "Удалить",
                                    middleActionText = "Отмена",
                                    topActionCallback = {
                                        currentTextNoteToEdit?.let{
                                            viewModel.deleteMediaNotes(
                                                listOf(it)
                                            )
                                        }
                                        mediaToolbarView.stopEditing()
                                        currentTextNoteToEdit = null
                                    }
                                )

                            }
                            return@setOnSendTextCallback false
                        }

            }
            setOnCancelEditingTextCallback {
//                changeRecyclerPadding(isGrowing = false)
                currentTextNoteToEdit = null
            }

            setOnStartRecordingCallback {
                releasePlayer()
            }

            setOnOpenCameraCallback {
                onAttemptToOpenCamera()
            }

            setOnOpenSketchCallback {
                findNavController().navigate(
                    R.id.action_mediaNotesFragment_to_mediaSketchFragment, bundleOf(
                        CURRENT_SHARD_ID to shardId
                    )
                )
            }

            setOnCompleteRecordingCallback { amplitudesList, speech, filePath ->
                val dbMediaNote = DbMediaNotes(
                    id = UUID.randomUUID().toString(),
                    shardId = shardId,
                    value = filePath,
                    mediaType = "voice",
                    order = System.currentTimeMillis(),
                    recognizedSpeechText = speech,
                    voiceAmplitudesList = amplitudesList
                )
                viewModel.saveDbMediaNotes(dbMediaNote)
            }

        }


        viewModel.getAllDbMediaNotesByShardId(shardId)?.observe(
            viewLifecycleOwner
        ) {
            isListEmpty = it.isEmpty()
            (requireParentFragment()).no_media_notes_tv.visibility =
                if (it.isEmpty()) View.VISIBLE else View.GONE
            val sortedList = it.sortedByDescending { mediaNote -> mediaNote.order }.reversed()
            media_notes_recycler_view.setData(sortedList.map {
                CustomRecyclerView.MediaNote(
                    id = it.id,
                    mediaType = when (it.mediaType) {
                        "sketch" -> CustomRecyclerView.MediaItemTypes.TYPE_SKETCH
                        "voice" -> CustomRecyclerView.MediaItemTypes.TYPE_VOICE
                        "photo" -> CustomRecyclerView.MediaItemTypes.TYPE_PHOTO
                        "text" -> CustomRecyclerView.MediaItemTypes.TYPE_TEXT
                        else -> CustomRecyclerView.MediaItemTypes.TYPE_TEXT
                    },
                    value = it.value,
                    recognizedSpeechText = it.recognizedSpeechText,
                    voiceAmplitudesList = it.voiceAmplitudesList ?: listOf(),
                    imageNoteText = it.imageNoteText,
                    timeStamp = it.order
                )
            })
        }


    }

    private fun onAttemptToOpenCamera() {
        if (checkCameraPermissionGranted()) {
            if (checkStoragePermission()) {
                findNavController().navigate(
                    R.id.action_mediaNotesFragment_to_cameraCaptureFragment,
                    bundleOf(
                        CURRENT_SHARD_ID to shardId
                    )
                )
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_CODE_STORAGE
                    )
                }
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CODE_CAMERA
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        no_media_notes_tv.visibility = if (isListEmpty) View.VISIBLE else View.GONE
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.pause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    fun releasePlayer() {
        media_notes_recycler_view.releasePlayer()
    }

    private fun onGalleryImagePicked(imagePath: String) {
        findNavController().navigate(
            R.id.action_mediaNotesFragment_to_imageCropFragment,
            bundleOf(
                CURRENT_SHARD_ID to shardId,
                EXISTING_PHOTO_PATH to imagePath,
                IS_NEED_TO_SAVE_TO_GALLERY to false
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_REQUEST_CODE_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {

            val selectedImage: Uri = data.data!!
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor = requireActivity().contentResolver.query(
                selectedImage,
                filePathColumn, null, null, null
            )!!
            cursor.moveToFirst()
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()
            onGalleryImagePicked(picturePath)

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE_CAMERA -> {
                if (checkCameraPermissionGranted()) {
                    if (checkStoragePermission()) {
                        findNavController().navigate(
                            R.id.action_mediaNotesFragment_to_cameraCaptureFragment,
                            bundleOf(
                                CURRENT_SHARD_ID to shardId
                            )
                        )
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            requestPermissions(
                                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                PERMISSION_REQUEST_CODE_STORAGE
                            )
                        }
                    }
                } else {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.permission_request_cancelled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            PERMISSION_REQUEST_CODE_STORAGE -> {
                if (checkCameraPermissionGranted() && checkStoragePermission()) {
                    findNavController().navigate(
                        R.id.action_mediaNotesFragment_to_cameraCaptureFragment,
                        bundleOf(
                            CURRENT_SHARD_ID to shardId
                        )
                    )
                } else {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.permission_request_cancelled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }
}

fun CustomRecyclerView.MediaNote.toDbMediaNote(): DbMediaNotes {
    return DbMediaNotes(
        id = id,
        shardId = SHARD_ID,
        value = value,
        mediaType = when (mediaType) {
            CustomRecyclerView.MediaItemTypes.TYPE_SKETCH -> "sketch"
            CustomRecyclerView.MediaItemTypes.TYPE_VOICE -> "voice"
            CustomRecyclerView.MediaItemTypes.TYPE_PHOTO -> "photo"
            CustomRecyclerView.MediaItemTypes.TYPE_TEXT -> "text"
        },
        order = timeStamp,
        recognizedSpeechText = recognizedSpeechText,
        imageNoteText = imageNoteText,
        voiceAmplitudesList = voiceAmplitudesList

    )
}
