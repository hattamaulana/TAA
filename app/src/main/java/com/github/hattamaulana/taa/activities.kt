package com.github.hattamaulana.taa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

class UploadActivity : AppCompatActivity(), Callback<String> {

    private lateinit var mFileImage: File
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)

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

    override fun onFailure(call: Call<String>, t: Throwable) {
        progressBar.visibility = View.GONE
        Log.i(TAG, "handleClickBtnUpload: onResponse: Network Failure")
    }

    override fun onResponse(call: Call<String>, response: Response<String>) {
        progressBar.visibility = View.GONE

        Log.i(TAG, "handleClickBtnUpload: onResponse: isSucces= ${response.isSuccessful}")
        Log.i(TAG, "handleClickBtnUpload: onResponse: isSucces= ${response.body()}")

        if (response.isSuccessful) {
            sharedPreferences.edit().apply {
                putString(KEY_NAME, response.body())
                apply()
            }
            finish()
        }
    }

    private val handleClickBtnUpload = View.OnClickListener {
        progressBar.visibility = View.VISIBLE

        try {
            val requestFile: RequestBody = RequestBody.create(
                MediaType.parse("image/*"), mFileImage
            )
            val body = MultipartBody.Part
                .createFormData("file", mFileImage.name, requestFile)
            ServiceGenerator.createService()?.upload(body)?.enqueue(this)
        } catch (e: UninitializedPropertyAccessException) {
            Toast.makeText(this, "Please Select Image", Toast.LENGTH_SHORT)
                .show()
        }
    }
}