package com.example.mynotes

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.ui.MainActivity
import com.example.mynotes.ui.noteslist.NotesListAdapter
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


private const val QUICK_NOTE = "Quick note"
private const val DESCRIPTION = "Description"

@RunWith(AndroidJUnit4::class)
class UiTests : BaseTest(), KoinComponent {

    private val appDatabase: AppDatabase by inject()
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {

        appDatabase.clearAllTables()

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            val toolbar =
                it.findViewById<androidx.appcompat.widget.Toolbar>(R.id.notes_list_toolbar)
            it.setSupportActionBar(toolbar)
        }
    }

    @Test
    fun addQuickNote() {
        typeText(R.id.notes_list_text_input_edittext_insert, QUICK_NOTE)
        click(R.id.notes_list_button_add_note)

        onView(withId(R.id.notes_list_recycler_view))
            .perform(
                scrollTo<NotesListAdapter.ViewHolder>(
                    hasDescendant(withText(QUICK_NOTE))
                )
            ).check(matches(isDisplayed()))
    }

    @Test
    fun addEmptyQuickNoteShouldShowSnackBar() {
        typeText(R.id.notes_list_text_input_edittext_insert, "")
        click(R.id.notes_list_button_add_note)

        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.notes_list_fragment_toast_empty_note)))
    }

    @Test
    fun clickOnExpandButtonAndCreateANoteFromNewNoteFragment() {
        clickOnExpandButton()

        typeText(R.id.new_note_text_input_edittext_title, QUICK_NOTE)
        typeText(R.id.new_note_text_input_edittext_description, DESCRIPTION)
        click(R.id.new_note_options_colors)
        click("Red")
        Espresso.pressBack()
        click(R.id.fragment_new_note_menu_item_save_note)

        onView(withId(R.id.notes_list_root))
            .check(matches(isDisplayed()))
    }
}