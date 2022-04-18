package ru.scheduled.mediaattachtest

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.github.florent37.kotlin.pleaseanimate.please
import kotlinx.android.synthetic.main.activity_main.*
import ru.scheduled.mediaattachtest.ui.base.BaseActivity


class MainActivity : BaseActivity() {

    var navHostFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        please(duration = 0L) {
            animate(loaderCl) toBe {
                invisible()
                leftOf(mainCl)
            }
        }.start()

    }

    fun showLoader(){
        please(duration = 0L) {
            animate(loaderCl) toBe {
                originalPosition()
            }
        }.thenCouldYou(150L) {
            animate(loaderCl) toBe {
                visible()
            }
        }.start()
    }
    fun hideLoader(){
        please(duration = 150L) {
            animate(loaderCl) toBe {
                invisible()
            }
        }.thenCouldYou(150L) {
            animate(loaderCl) toBe {
                leftOf(mainCl)
            }
        }.start()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val frag = navHostFragment?.childFragmentManager?.fragments?.lastOrNull()
        frag?.let{
            frag.onActivityResult(requestCode,resultCode,data)
        }
    }
}