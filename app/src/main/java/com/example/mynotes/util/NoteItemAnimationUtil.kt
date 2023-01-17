package com.example.mynotes.util

import android.content.Context
import android.transition.Transition
import android.transition.TransitionInflater
import com.example.mynotes.R

class NoteItemAnimationUtil {

    companion object {
        fun setMoveTransitionAnimation(context: Context): Transition? {
            val animation = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
            animation.duration = 300L

            return animation
        }

        fun setSlideFromTopBottomTransition(context: Context): Transition? {
            val animation = TransitionInflater.from(context)
                .inflateTransition(R.transition.slide_top_to_bottom)

            animation.duration = 300L

            return animation
        }

        fun setSlideBottomTopTransition(context: Context): Transition? {
            val animation = TransitionInflater.from(context)
                .inflateTransition(R.transition.slide_top_to_bottom)

            animation.duration = 300L

            return animation
        }

    }
}