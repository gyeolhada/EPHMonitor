package com.example.ephmonitor.utils

import android.content.Context
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope

object ChaquopyUtil {
    private var instance: Python? = null
    private var exerciseModel: PyObject? = null
    private var getWaveModel: PyObject? = null
    private var tryModel: PyObject? = null
    private var audioObj: PyObject? = null

    // 启动 Python 环境
    fun setup(context: Context) {
        try {
            // 如果已启动 Python，先销毁再重新启动
            destroy()
            // 启动 Python 环境并输出日志
            Log.d("ChaquopyUtil", "Starting Python environment...")
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(context))
                Log.d("ChaquopyUtil", "Python environment started.")
            } else {
                Log.d("ChaquopyUtil", "Python environment is already started.")
            }

            instance = Python.getInstance()
            // 获取 Python 模块
            exerciseModel = instance?.getModule("exercise_run")
            tryModel = instance?.getModule("try")
            getWaveModel = instance?.getModule("get_wave")
            Log.d("ChaquopyUtil", "Python environment started and modules loaded.")
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "Error during setup: $e")
        }
    }

    fun setupOffline(context: Context) {
        if (Python.isStarted()) return
        Python.start(AndroidPlatform(context))
        instance = Python.getInstance()
        tryModel = instance?.getModule("try")
        getWaveModel = instance?.getModule("get_wave")
    }

    fun testOffline(): Pair<List<Float>, List<Float>>? {
        val prehd = getWaveModel?.callAttr("run")
        try {
            Log.d("ChaquopyUtil", "prehd=$prehd")
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "e=$prehd")
        }
        val result = tryModel?.callAttr("run")
        Log.d("ChaquopyUtil", "res=$result")
        return try {
            val ecg = result?.asList()?.get(0)?.asList()?.map { it.toFloat() }
            val breathing = result?.asList()?.get(1)?.asList()?.map { it.toFloat() }
            Pair(ecg ?: emptyList(), breathing ?: emptyList())
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "Error retrieving waveforms: $e")
            null
        }
    }

    fun runRecordAndSave(callback: () -> Unit) {
        audioObj = exerciseModel?.callAttr("run")
        try {
            Log.d("ChaquopyUtil", "audioObj=$audioObj")
            callback()  // 确保回调被调用，表示操作完成
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "e=$audioObj")
            callback()  // 即使出错，也调用回调
        }
    }

    fun getWaveform(): Pair<List<Float>, List<Float>>? {
        val prehd = getWaveModel?.callAttr("run")
        try {
            Log.d("ChaquopyUtil", "prehd=$prehd")
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "e=$prehd")
        }
        val result = exerciseModel?.callAttr("get_waveforms", audioObj)
        Log.d("ChaquopyUtil", "res=$result")
        return try {
            val ecg = result?.asList()?.get(0)?.asList()?.map { it.toFloat() }
            val breathing = result?.asList()?.get(1)?.asList()?.map { it.toFloat() }
            Pair(ecg ?: emptyList(), breathing ?: emptyList())
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "Error retrieving waveforms: $e")
            null
        }
    }

    // 清理 Python 环境资源
    fun destroy() {
        try {
            // 清理模块资源
            exerciseModel = null
            getWaveModel = null
            tryModel = null
            // 假设 Python 本身不能直接 stop，这里我们将实例置为 null
            instance = null
            Log.d("ChaquopyUtil", "Python environment resources cleared.")
        } catch (e: Exception) {
            Log.e("ChaquopyUtil", "Error during cleanup: $e")
        }
    }
}

