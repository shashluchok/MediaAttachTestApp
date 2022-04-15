package ru.leadfrog.common.ui.camera_capture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.android.synthetic.main.fragment_start.*
import org.koin.android.ext.android.inject
import ru.leadfrog.common.ActivityRequestCodes
import ru.leadfrog.common.PermissionRequestCodes
import ru.scheduled.mediaattachtest.R
import ru.scheduled.mediaattachtest.ui.base.BaseFragment
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.CURRENT_SHARD_ID
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.EXISTING_PHOTO_PATH
import ru.scheduled.mediaattachtest.ui.media_attachments.MediaConstants.Companion.IS_NEED_TO_SAVE_TO_GALLERY
import ru.scheduled.mediaattachtest.ui.media_attachments.camera_capture.CameraCaptureStates
import ru.scheduled.mediaattachtest.ui.media_attachments.camera_capture.CameraCaptureViewModel

class StartFragment: BaseFragment() {
    override val layoutResId: Int
        get() = R.layout.fragment_start

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_mediaNotesFragment)
        }
    }



}