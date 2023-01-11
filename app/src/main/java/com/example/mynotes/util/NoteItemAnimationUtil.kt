package com.example.mynotes.util

import android.content.Context
import android.transition.Transition
import android.transition.TransitionInflater

class NoteItemAnimationUtil {

    companion object {
        fun setAnimation(context: Context): Transition? {
            val animation = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
            animation.duration = 200L

            return animation
        }

    }
}