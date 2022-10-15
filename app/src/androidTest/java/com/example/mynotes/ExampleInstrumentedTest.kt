package com.example.mynotes

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

// TODO: instrumentation tests for quickly adding note from NoteListFragment
// TODO: instrumentation tests for start adding note from NoteListFragment then opens the NewNotesFragment to finish
// TODO: instrumentation tests for adding note from NewNotesFragment
// TODO: instrumentation tests for editing note

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.mynotes", appContext.packageName)
    }
}