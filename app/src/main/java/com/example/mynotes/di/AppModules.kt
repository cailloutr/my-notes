package com.example.mynotes.di

import androidx.room.Room
import com.example.mynotes.MyNotesApplication
import com.example.mynotes.database.AppDatabase
import com.example.mynotes.database.dao.NoteDao
import com.example.mynotes.database.migrations.MyNotesMigrations
import com.example.mynotes.database.repository.NotesRepository
import com.example.mynotes.ui.newnote.NewNoteFragment
import com.example.mynotes.ui.noteslist.NotesListFragment
import com.example.mynotes.ui.trash.TrashFragment
import com.example.mynotes.ui.viewModel.NotesListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    factory<NewNoteFragment> { NewNoteFragment() }
    factory<NotesListFragment> { NotesListFragment() }
    factory<TrashFragment> { TrashFragment() }
}

val viewModelModule = module {
    viewModel<NotesListViewModel> { NotesListViewModel(get(), get()) }
}

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
}

val applicationModule = module {
    single<MyNotesApplication> { MyNotesApplication() }
}