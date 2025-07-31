package com.example.ephmonitor.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock

object BatteryUtil {
    // 估算的电池容量，假设设备电池为 5000mAh，实际应根据设备电池规格进行调整
    private const val FULL_BATTERY_CAPACITY_MAH = 5000

    // 计算当前电量百分比
    fun getBatteryPercentage(context: Context): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return ((level / scale.toFloat()) * 100).toInt()
    }

    // 计算电池的耗电速率 (mAh/min)
    fun calculateDrainRate(context: Context, previousLevel: Int, previousTime: Long): Float {
        val currentLevel = getBatteryPercentage(context)
        val currentTime = SystemClock.elapsedRealtime() // 当前时间（毫秒）

        val timeDifferenceInMinutes = (currentTime - previousTime) / 1000f / 60f // 转换为分钟
        val levelDifference = previousLevel - currentLevel

        if (timeDifferenceInMinutes > 0 && levelDifference > 0) {
            val batteryCapacity = FULL_BATTERY_CAPACITY_MAH * (previousLevel / 100f)
            val drainRate = (batteryCapacity * (levelDifference / 100f)) / timeDifferenceInMinutes // mAh/min
            return drainRate
        }
        return 0f
    }
}
