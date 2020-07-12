package com.github.hattamaulana.taa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

const val TAG = "TEST_UPLOAD_APPS"
const val KEY_NAME = "KEY_NAME"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)

        sharedPreferences.getString(KEY_NAME, null)?.let {
            ServiceGenerator.createService()
                ?.get(it)
                ?.enqueue(object : retrofit2.Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.i(
                            TAG,
                            "handleClickBtnUpload: onResponse: Network Failure"
                        )
                    }

                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Log.i(
                            TAG, """
                                handleClickBtnUpload: onResponse: isSucces= ${response.isSuccessful}
                            """.trimIndent()
                        )

                        if (response.isSuccessful) {
                            finish()
                        }
                    }
                })
        }

        btnUpload.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }
}

class UploadActivity : AppCompatActivity() {

    private lateinit var mFileImage: File
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE)

        textView.text = "CLick Untuk Upload"
        textView.setOnClickListener {
            AlertDialog.Builder(this).chooseAction()
        }

        btnUpload.setOnClickListener(handleClickBtnUpload)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        data?.data?.let {
            val bitmap = getBitmap(it)!!
            mFileImage = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "${ System.currentTimeMillis() }_selectedImg.jpg"
            )

            mFileImage.convertBitmapToFile(bitmap)
            imageView.background = BitmapDrawable(resources, bitmap)
        }
    }

    private val handleClickBtnUpload = View.OnClickListener {
        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse("image/*"), mFileImage
        )

        val body = MultipartBody.Part
            .createFormData("file", mFileImage.name, requestFile)

        ServiceGenerator.createService()?.upload(body)?.enqueue(object : retrofit2.Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.i(
                    TAG,
                    "handleClickBtnUpload: onResponse: Network Failure"
                )
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.i(TAG, """
                        handleClickBtnUpload: onResponse: isSucces= ${response.isSuccessful}
                    """.trimIndent()
                )

                if (response.isSuccessful) {
                    sharedPreferences.edit().apply {
                        putString(KEY_NAME, response.body())
                        apply()
                    }

                    finish()
                }
            }
        })
    }
}