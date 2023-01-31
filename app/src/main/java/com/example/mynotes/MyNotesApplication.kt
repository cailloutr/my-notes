package com.example.mynotes

import android.app.Application
import com.example.mynotes.di.applicationModule
import com.example.mynotes.di.databaseModule
import com.example.mynotes.di.uiModule
import com.example.mynotes.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyNotesApplication: Application() {

//    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyNotesApplication)
            modules(
                listOf(
                    uiModule,
                    viewModelModule,
                    databaseModule,
                    applicationModule
                )
            )
        }
    }
}