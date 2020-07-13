package com.github.hattamaulana.taa

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

var photoFile: File? = null
var photoUri: Uri? = null

fun AlertDialog.Builder.chooseAction(activity: Activity) {
    arrayOf("Take Photo", "Choose from Library", "Cancel").also {
        setItems(it) { dialog, i ->
            when(it[i]) {
                "Choose from Library" -> context.requestStoragePermission(activity) {
                    activity.accessGalery()
                }

                "Cancel" -> dialog.dismiss()
            }
        }
    }

    show()
}

@Throws(IOException::class)
fun Context.getBitmap(uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, uri)
        )
    } else {
        return contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it)
        }
    }
}

@Throws(IOException::class)
fun File.convertBitmapToFile(bitmap: Bitmap) {
    createNewFile()
    ByteArrayOutputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, it)
        FileOutputStream(this).apply {
            write(it.toByteArray())
            flush()
            close()
        }
    }
}
