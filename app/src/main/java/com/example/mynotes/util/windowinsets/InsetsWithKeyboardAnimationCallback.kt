package com.example.mynotes.util.windowinsets

import android.view.View
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.graphics.Insets

class InsetsWithKeyboardAnimationCallback(
    private val view: View
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {
    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: MutableList<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        val insetsIme = insets.getInsets(WindowInsetsCompat.Type.ime())
        val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        val diff = Insets.subtract(insetsIme, systemInsets).let {
            Insets.max(it, Insets.NONE)
        }

        view.translationX = (diff.left - diff.right).toFloat()
        view.translationY = (diff.top - diff.bottom).toFloat()

        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // We reset the translation values after the animation has finished
        view.translationX = 0f
        view.translationY = 0f
    }
}