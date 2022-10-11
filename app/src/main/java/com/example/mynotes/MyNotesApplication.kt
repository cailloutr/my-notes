package com.example.mynotes

import android.app.Application
import com.example.mynotes.database.AppDatabase

class MyNotesApplication: Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}