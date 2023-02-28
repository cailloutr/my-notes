package com.example.mynotes.util.windowinsets

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*

class WindowUtil {

    companion object {

        fun setupEdgeToEdgeLayout(
            window: Window,
            toolbar: Toolbar,
            footer: ConstraintLayout
        ) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            implementsSystemBarInsets(toolbar, footer)
            window.navigationBarColor = android.R.color.transparent
        }


        fun implementsStatusBarInsets(toolbar: Toolbar) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply the insets as a margin to the view. Here the system is setting
                // only the bottom, left, and right dimensions, but apply whichever insets are
                // appropriate to your layout. You can also update the view padding
                // if that's more appropriate.


                view.updatePadding(
                    top = insets.top,
                )

                // Return CONSUMED if you don't want want the window insets to keep being
                // passed down to descendant views.
                WindowInsetsCompat.CONSUMED
            }
        }

        private fun implementsSystemBarInsets(toolbar: View, footer: View) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply the insets as a margin to the view. Here the system is setting
                // only the bottom, left, and right dimensions, but apply whichever insets are
                // appropriate to your layout. You can also update the view padding
                // if that's more appropriate.


                view.updatePadding(
                    top = insets.top,
                )

                // Return CONSUMED if you don't want want the window insets to keep being
                // passed down to descendant views.
                WindowInsetsCompat.CONSUMED
            }
            ViewCompat.setOnApplyWindowInsetsListener(footer) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                // Apply the insets as a margin to the view. Here the system is setting
                // only the bottom, left, and right dimensions, but apply whichever insets are
                // appropriate to your layout. You can also update the view padding
                // if that's more appropriate.


                view.updateLayoutParams<MarginLayoutParams> {
                    bottomMargin = insets.bottom
                }

                // Return CONSUMED if you don't want want the window insets to keep being
                // passed down to descendant views.
                WindowInsetsCompat.CONSUMED
            }
        }
    }
}