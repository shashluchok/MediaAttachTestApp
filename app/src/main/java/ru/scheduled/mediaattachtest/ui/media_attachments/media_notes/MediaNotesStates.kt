package ru.scheduled.mediaattachtest.ui.media_attachments.media_notes

import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes


sealed class MediaNotesStates {
    data class ErrorState(val text: String) : MediaNotesStates()
    object MediaNoteRemovedState: MediaNotesStates()
    object MediaNoteSavedState: MediaNotesStates()
    data class MediaNoteUpdatedState(val mediaNoteId:String): MediaNotesStates()
    data class MediaNoteLoadedState(val dbMediaNotes: DbMediaNotes): MediaNotesStates()
}