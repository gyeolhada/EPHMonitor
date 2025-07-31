package com.example.ephmonitor

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ephmonitor.databinding.ActivityMainBinding
import com.example.ephmonitor.service.MusicService
import com.example.ephmonitor.service.UserService
import com.example.ephmonitor.utils.BatteryUtil
import com.example.ephmonitor.utils.ChaquopyUtil
import com.example.ephmonitor.utils.PermissionUtil
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var userService = MutableLiveData<UserService?>()
    private var musicService = MutableLiveData<MusicService?>()

    private val openAIService = OpenAIService("sk-c6fc3f5bfc2949d28963b23be78b0e0c")

    private val connectionUser = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as UserService.UserBinder
            userService.postValue(binder.getService())
            Log.i("UserService", "onServiceConnected${binder.getService()}")
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    private val connectionMusic = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MusicService.MusicBinder
            musicService.postValue(binder.getService())
            Log.i("MusicService", "onServiceConnected ${binder.getService()}")
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            musicService.value = null
            Log.i("MusicService", "onServiceDisconnected")
        }
    }

    private fun checkAndRequestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 及以上
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            // Android 10 及以下
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1001
            )
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查并请求外部存储权限（特别是 Android 11 以上）
        checkAndRequestStoragePermission()

        PermissionUtil.setUp(this)
        if (!PermissionUtil.checkPermission()) {
            Toast.makeText(this, "请允许APP访问所有文件，否则无法使用。", Toast.LENGTH_SHORT).show();
            this.finish()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取并打印电量
        val batteryPercentage = BatteryUtil.getBatteryPercentage(this)
        Log.d("BatteryStatus", "Current battery percentage: $batteryPercentage%")

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_mine, R.id.navigation_connect
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    supportActionBar?.hide()
                    navView.visibility = View.GONE
                }
                else -> {
                    supportActionBar?.hide()
                    navView.visibility = View.VISIBLE
                }
            }
        }
        bindMusicService()
    }

    fun getUserService(): MutableLiveData<UserService?> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(this, "请允许APP访问所有文件，否则无法使用。", Toast.LENGTH_SHORT)
                    .show();
                startActivity(Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            }
        }
        if (userService.value == null) {
            Log.i("UserService", "start service")
            Intent(this, UserService::class.java).also { intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else startService(intent)
                bindService(intent, connectionUser, Context.BIND_AUTO_CREATE)
            }
        }
        return userService
    }

    fun test(callback: () -> Unit) {
        // 确保 Chaquopy 已正确设置
        ChaquopyUtil.setup(this)

        // 执行音频录制并保存，录制完成后调用回调
        ChaquopyUtil.runRecordAndSave {
            // 音频数据录制和保存完成后执行回调
            callback()
        }

        // 进行音频流连接（可在录制过程中异步进行）
        //connectToAudioStream("http://192.168.137.115/")
    }
    fun connectToAudioStream(url: String) {
        // 创建 OkHttpClient 实例
        val client = OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        // 创建请求
        val request = Request.Builder()
            .url(url)
            .build()
        // 发送请求并处理响应
        Thread {
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // 成功连接并接收到音频流
                    Log.i("AudioStream", "成功连接音频流，状态码: ${response.code}")
                    // 在此处处理音频流，可能需要用 InputStream 来读取流
                } else {
                    Log.e("AudioStream", "连接失败，状态码: ${response.code}")
                }
            } catch (e: IOException) {
                Log.e("AudioStream", "音频流连接失败: ${e.message}")
            }
        }.start()
    }
    //----------
    fun testChart(): Pair<List<Float>, List<Float>>? {
        Log.i("TestChart", "Get Waveform")
        return ChaquopyUtil.getWaveform()
    }
    fun testChartOffline(): Pair<List<Float>, List<Float>>? {
        // 确保 Chaquopy 已正确设置
        ChaquopyUtil.setupOffline(this)
        Log.i("TestChart", "Get Waveform")
        return ChaquopyUtil.testOffline()
    }
    fun userLogout() {
        //停止服务
        if (userService.value != null) {
            userService.value!!.stopSelf()
            unbindService(connectionUser)
            userService.value = null
        }
    }
    // 绑定 MusicService
    private fun bindMusicService() {
        if (musicService.value == null) {
            Log.i("MusicService", "start music service")
            val intent = Intent(this, MusicService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            bindService(intent, connectionMusic, Context.BIND_AUTO_CREATE)
        }
    }
    // 解绑 MusicService
    private fun unbindMusicService() {
        if (musicService.value != null) {
            unbindService(connectionMusic)
            musicService.value = null
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Activity 销毁时解绑 MusicService
        unbindMusicService()
    }

    //*****协程
    // 🌟 1. 修改 `test()` 让它支持 `suspend`
    suspend fun test1() {
        withContext(Dispatchers.IO) {
            // 录制音频并挂起等待完成
            ChaquopyUtil.setup(this@MainActivity)
            suspendCancellableCoroutine<Unit> { cont ->
                ChaquopyUtil.runRecordAndSave {
                    cont.resume(Unit) {} // 录制完成，恢复协程
                }
            }
        }
    }
    // 🌟 2. 修改 `testChart()` 让它支持 `suspend`
    suspend fun testChart1(): Pair<List<Float>, List<Float>>? {
        return withContext(Dispatchers.IO) {
            ChaquopyUtil.getWaveform() // 确保在后台线程执行
        }
        ChaquopyUtil.destroy()
    }

    fun requestFitnessAdvice(
        heartRate: List<Float>,
        breathRate: List<Float>,
        caloriesBurned: Float,
        exercise_type: String,
        callback: (String) -> Unit
    ) {
        openAIService.getFitnessAdvice(heartRate, breathRate, caloriesBurned, exercise_type, callback)
    }
}