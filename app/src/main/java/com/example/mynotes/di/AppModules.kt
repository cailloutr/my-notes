package com.example.mynotes.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.dao.NoteDao
import com.example.mynotes.database.migrations.MyNotesMigrations
import com.example.mynotes.database.repository.InternalStorageRepository
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.database.repository.UserPreferencesRepository
import com.example.mynotes.ui.MainActivity
import com.example.mynotes.ui.newnote.NewNoteFragment
import com.example.mynotes.ui.noteslist.NotesListFragment
import com.example.mynotes.ui.trash.TrashFragment
import com.example.mynotes.ui.viewModel.NotesListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    factory<NewNoteFragment> { NewNoteFragment() }
    factory<NotesListFragment> { NotesListFragment() }
    factory<TrashFragment> { TrashFragment() }
    single<MainActivity> { MainActivity() }
}

val viewModelModule = module {
    viewModel<NotesListViewModel> { NotesListViewModel(get(), get(), get()) }
}

private val USER_PREFERENCES = "user_preferences"

val databaseModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "app_database"
        )
            .addMigrations(*MyNotesMigrations.ALL_MIGRATIONS)
            .build()
    }
    single<NoteDao> { get<AppDatabase>().notesDao() }
    single<MyNotesMigrations> { MyNotesMigrations() }
    single<NotesRepository> { NotesRepository(get()) }
    single<UserPreferencesRepository> { UserPreferencesRepository(get()) }
    single<InternalStorageRepository> { InternalStorageRepository(get()) }
    single<DataStore<Preferences>> { PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() }
        ),
        migrations = listOf(SharedPreferencesMigration(get(), "default")),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { get<Context>().preferencesDataStoreFile(USER_PREFERENCES) }
    ) }
}

val applicationModule = module {
    single<MyNotesApplication> { MyNotesApplication() }
}