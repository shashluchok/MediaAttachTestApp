package ru.scheduled.mediaattachtest.ui.media_attachments.media_notes

import ru.scheduled.mediaattachtest.db.media_uris.DbMediaNotes


sealed class MediaNotesStates {
    object ErrorState: MediaNotesStates()
    object MediaNoteRemovedState: MediaNotesStates()
    object MediaNoteSavedState: MediaNotesStates()
    object MediaNoteUpdatedState: MediaNotesStates()
    data class MediaNoteLoadedState(val dbMediaNotes: DbMediaNotes): MediaNotesStates()
}