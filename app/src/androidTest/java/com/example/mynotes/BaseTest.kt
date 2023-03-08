package com.example.mynotes

import androidx.annotation.IdRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers

open class BaseTest {

    fun typeText(@IdRes id: Int, text: String) {
        Espresso.onView(ViewMatchers.withId(id))
            .perform(ViewActions.replaceText(text))
        ViewActions.closeSoftKeyboard()
    }

    fun click(@IdRes id: Int) {
        Espresso.onView(ViewMatchers.withId(id))
            .perform(ViewActions.click())
    }

    fun click(text: String) {
        Espresso.onView(ViewMatchers.withText(text))
            .perform(ViewActions.click())
    }

    fun clickOnExpandButton() {
        Espresso.onView(
            ViewMatchers.withContentDescription(
                R.string.fragment_note_text_input_end_icon_content_description
            )
        ).perform(ViewActions.click())
    }
}