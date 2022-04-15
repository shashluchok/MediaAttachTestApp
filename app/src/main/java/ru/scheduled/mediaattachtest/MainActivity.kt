package ru.scheduled.mediaattachtest

import android.os.Bundle
import ru.scheduled.mediaattachtest.ui.base.BaseActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}