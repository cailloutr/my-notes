package com.example.mynotes.database.repository

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class InternalStorageRepository {

    private val TAG = "StorageRepository"
    private val ALBUM_NAME = "notes"
    private val ALBUM_TEMP = "temp"
    private val COMPRESS_QUALITY = 100

    fun createTempImageFile(context: Context, id: String): File {
        clearCache(context)
        return File.createTempFile("JPEG_${id}_", ".jpg", context.cacheDir)
    }

    private fun clearCache(context: Context) {
        context.cacheDir.deleteRecursively()
    }

    private fun getAppSpecificAlbumStorageDir(context: Context): File {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(
            context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            ), ALBUM_NAME
        )
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created")
        }
        return file
    }

    fun deleteImageInAppSpecificAlbumStorageDir(context: Context, id: String) {
        val path = getImagePath(context, id)
        val file = File(path)

        if (file.exists()) {
            try {
                val fileDeleted = file.delete()
                Log.d(TAG, "deleteImageInAppSpecificAlbumStorageDir: $fileDeleted")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveImageInAppSpecificAlbumStorageDir(
        bitmap: Bitmap?,
        path: String
    ): String {
        var baos: OutputStream? = null
        val file = File(path)

        try {
            baos = FileOutputStream(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bitmap?.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, baos)

        try {
            baos?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            baos?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return path
    }

    fun getImagePath(context: Context, id: String) =
        "${getAppSpecificAlbumStorageDir(context)}/$id.jpg"
}