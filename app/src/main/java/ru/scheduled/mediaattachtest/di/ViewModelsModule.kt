package ru.scheduled.mediaattachtest.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.scheduled.mediaattachtest.ui.media_attachments.camera_capture.CameraCaptureViewModel
import ru.scheduled.mediaattachtest.ui.media_attachments.camera_capture.image_crop.ImageCropViewModel
import ru.scheduled.mediaattachtest.ui.media_attachments.media_image_viewer.MediaImageViewerViewModel
import ru.scheduled.mediaattachtest.ui.media_attachments.media_notes.MediaNotesViewModel
import ru.scheduled.mediaattachtest.ui.media_attachments.media_sketch.MediaSketchViewModel


val viewModelsModule = module {

    viewModel { ImageCropViewModel(get()) }

    viewModel { CameraCaptureViewModel(get()) }

    viewModel { MediaImageViewerViewModel(get()) }

    viewModel { MediaNotesViewModel(get()) }

    viewModel { MediaSketchViewModel(get()) }

}