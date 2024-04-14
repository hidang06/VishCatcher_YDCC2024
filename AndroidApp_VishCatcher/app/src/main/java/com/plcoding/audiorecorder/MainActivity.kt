package com.plcoding.audiorecorder

import ApiService
import android.Manifest
import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.plcoding.audiorecorder.playback.AndroidAudioPlayer
import com.plcoding.audiorecorder.record.AndroidAudioRecorder
import com.plcoding.audiorecorder.ui.theme.AudioRecorderTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.FileInputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    private val BASE_URL = "https://jsonplaceholder.typicode.com/comments"
    private val TAG: String = "CHECK_RESPONSE"


    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private var audioFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )
        setContent {
            AudioRecorderTheme {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Image(
                        painter = painterResource(R.drawable.background), // Thay thế bằng resource ID của hình ảnh của bạn
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds // Thay đổi tỉ lệ của hình ảnh cho phù hợp với kích thước màn hình
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            File(cacheDir, "audio.mp3").also {
                                recorder.start(it)
                                audioFile = it
                            }
                        }) {
                            Text(text = "Start recording")
                        }
                        Button(onClick = {
                            recorder.stop()
                        }) {
                            Text(text = "Stop recording")
                        }
                        Button(onClick = {
                            player.playFile(audioFile ?: return@Button)
                        }) {
                            Text(text = "Play")
                        }
//                        Button(onClick = {
//                            player.stop()
//                        }) {
//                            Text(text = "Stop playing")
//                        }
                        Button(onClick = {
//                        sendGetRequest()
//                        getComments()
                            uploadAudioToServer(audioFile)
                        }) {
                            Text(text = "Scan Audio")
                        }
                    }
                }
            }
        }
    }
    private fun uploadAudioToServer(file: File?) {
        file ?: return
        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.28.240.209:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("audio", file.name, requestFile)

        val call = apiService.uploadAudio(body)
        call.enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    result?.let {
                        when (it) {
                            0 -> Log.d("Upload", "Upload successful, response is 0")
//                            1 -> Log.d("Upload", "Upload successful, response is 1")
                            1 -> {
                                // Hiển thị pop-up khi it = 1
                                val mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.alert)
                                mediaPlayer.start()

                                val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
                                alertDialogBuilder.setTitle("Cảnh báo!")
                                alertDialogBuilder.setMessage("Nội dung có thể là cuộc gọi lừa đảo.")
                                alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
                                    // Xử lý sự kiện khi người dùng nhấn nút OK
//                                    finish()
                                }
                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }
                            else -> Log.e("Upload", "Invalid response value")
                        }
                    } ?: Log.e("Upload", "Response body is null")
                } else {
                    Log.e("Upload", "Response is not successful")
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("Upload", "Error: ${t.message}")
            }
        })
    }
//    @OptIn(DelicateCoroutinesApi::class)
//    private fun getComments() {
//        // Khởi tạo Retrofit
//
//        val api = Retrofit.Builder()
////            .baseUrl("http://172.28.240.209:5000/") // Thay đổi địa chỉ server nếu cần
//            .baseUrl("https://jsonplaceholder.typicode.com/") // Thay đổi địa chỉ server nếu cần
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//        api.getComments().enqueue(object : Callback<List<Comments>> {
//            override fun onResponse(p0: Call<List<Comments>>, p1: Response<List<Comments>>) {
//                if (p1.isSuccessful) {
//                    p1.body()?.let {
//                        for (comment in it) {
//                            Log.i(TAG, "on response: ${comment.body}")
//                        }
//                    }
//                }
//            }
//
//            override fun onFailure(p0: Call<List<Comments>>, p1: Throwable) {
//                Log.i(TAG, "on response: ${p1.message}")
//            }
//        })
//    }

//        val apiService = api.create(ApiService::class.java)
        // Gửi yêu cầu Retrofit để thực hiện yêu cầu GET
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val response = apiService.getAudio().execute()
//                if (response.isSuccessful) {
//                    Log.d(TAG, "GET Request Successful")
//                    // Xử lý dữ liệu ở đây nếu cần
//                } else {
//                    Log.e(TAG, "GET Request Failed")
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error: ${e.message}")
//            }
//        }
//    }
}