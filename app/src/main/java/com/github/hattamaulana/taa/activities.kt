package com.github.hattamaulana.taa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import java.io.File

const val TAG = "TEST_UPLOAD_APPS"
const val KEY_NAME = "KEY_NAME"

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)

        btnUpload.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.getString(KEY_NAME, null)?.let {
            imageView.visibility = View.VISIBLE
            textView.visibility = View.GONE
            Glide.with(this)
                .load("$BASE_URL/storage/$it")
                .into(imageView)
        }
    }
}

class UploadActivity : AppCompatActivity(), RequestObserverDelegate {

    private lateinit var mFileImage: File
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)

        supportActionBar?.title = "Upload Photo"
        progressBar.visibility = View.GONE
        textView.text = "CLick Untuk Upload"
        textView.setOnClickListener {
            AlertDialog.Builder(this).chooseAction(this)
        }

        btnUpload.setOnClickListener(handleClickBtnUpload)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        val bitmap: Bitmap? = when(requestCode) {
            REQUEST_ACCESS_CAMERA -> photoUri?.let { getBitmap(it) }
            REQUEST_ACCESS_GALERY -> data?.data?.let {
                getBitmap(it)
            }

            else -> null
        }

        bitmap?.let {
            mFileImage = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "${ System.currentTimeMillis() }_selectedImg.jpg"
            )
            mFileImage.convertBitmapToFile(bitmap)

            imageView.setImageBitmap(bitmap)
            imageView.visibility = View.VISIBLE
            textView.visibility = View.GONE

            Log.i(TAG, "onActivityResult: file=${ mFileImage.name }")
            Log.i(TAG, "onActivityResult: file=${ mFileImage.path }")
        }
    }

    private val handleClickBtnUpload = View.OnClickListener {
        progressBar.visibility = View.VISIBLE

        Log.i(TAG, "handleClickBtnUpload: isSucces= ${ mFileImage.path }")

        MultipartUploadRequest(this, serverUrl = "$BASE_URL/api").apply {
            setMethod("POST")
            addFileToUpload(
                filePath = mFileImage.path,
                parameterName = "file"
            )
            startUpload()
            subscribe(this@UploadActivity, this@UploadActivity,
                this@UploadActivity)
        }
    }

    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
        update()
        finish()
    }

    override fun onCompletedWhileNotObserving() {
    }

    override fun onError(context: Context, uploadInfo: UploadInfo, exception: Throwable) {
        update()
        finish()
    }

    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
    }

    override fun onSuccess(
        context: Context,
        uploadInfo: UploadInfo,
        serverResponse: ServerResponse
    ) {
        val response = serverResponse.bodyString.replace("\"", "")
        Log.i(TAG, "handleClickBtnUpload: isSucces= $response")
        update(response)
        finish()
    }

    private fun update(name: String? = null) {
        sharedPreferences.edit().apply {
            putString(KEY_NAME, name)
            apply()
        }
    }
}