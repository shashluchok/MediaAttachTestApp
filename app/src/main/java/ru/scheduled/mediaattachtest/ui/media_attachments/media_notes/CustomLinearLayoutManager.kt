package ru.scheduled.mediaattachtest.ui.media_attachments.media_notes

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
    private var isScrollEnabled = true
    fun setScrollEnabled(flag: Boolean) {
        isScrollEnabled = flag
    }

    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }
}