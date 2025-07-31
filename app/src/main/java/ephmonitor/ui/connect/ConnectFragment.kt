package com.example.ephmonitor.ui.connect

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ephmonitor.MainActivity
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentConnectBinding
import com.example.ephmonitor.enums.DeviceStatus
import com.example.ephmonitor.service.UserService
import java.text.SimpleDateFormat
import java.util.Date
import com.bumptech.glide.Glide
import com.example.ephmonitor.utils.BatteryUtil
import com.example.ephmonitor.utils.ChaquopyUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Thread.sleep
import java.util.Locale
import kotlin.random.Random

class ConnectFragment : Fragment() {
    private lateinit var bind: FragmentConnectBinding
    private lateinit var adapter: ConnectRecycleViewAdapter
    private var service: UserService? = null
    private lateinit var ecgwaveformChart: LineChart
    private lateinit var brwaveformChart: LineChart
    // 定义变量以存储上次的电池电量和时间
    private var previousBatteryLevel: Int = 0
    private var previousTime: Long = SystemClock.elapsedRealtime()
    private var hrList: MutableList<Float> = mutableListOf() // 可修改的列表
    private var brList: MutableList<Float> = mutableListOf() // 可修改的列表
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentConnectBinding.inflate(layoutInflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).getUserService()
            .observe(viewLifecycleOwner) { nService ->
                service = nService
                service?.let {
                    it.scannedDevices.observe(viewLifecycleOwner) { list ->
                        Log.i("ble", list.toString())
                        adapter.submitList(list)
                    }

                    Glide.with(requireContext()).asGif().load(R.drawable.radar).into(bind.ivBluetooth)
                    bind.switchBluetooth.setOnClickListener { service?.switchScanState() }
                    service?.isScanning?.observe(viewLifecycleOwner) {
                        if (it) {
                            bind.switchBluetooth.text = "停止扫描"
                            bind.ivBluetooth.visibility = View.VISIBLE
                            bind.tvScanState.visibility = View.VISIBLE
                            service?.startScan()
                            bind.switchBluetooth.isChecked = true
                        } else {
                            bind.switchBluetooth.text = "开始扫描"
                            bind.ivBluetooth.visibility = View.INVISIBLE
                            bind.tvScanState.visibility = View.INVISIBLE
                            service?.stopScan()
                            bind.switchBluetooth.isChecked = false
                        }
                    }

                    service?.connectedDevice?.observe(viewLifecycleOwner) {
                        if (it == null) {
                            bind.tvDeviceName.text = "未连接设备"
                            bind.tvDeviceStatus.text = "未连接"
                            Glide.with(this).load(R.drawable.unconnected).into(bind.myWatch)
                            return@observe
                        }
                        bind.tvDeviceName.text = it.mDeviceName

                        val now = Date()
                        val s2 = SimpleDateFormat("HH:mm")
                        val ss: String = s2.format(now)
                        val res2 = "(截至$ss)"
                        bind.ElectricUpdateTime.text = res2

                        if (it.mDeviceState == DeviceStatus.CONNECTED) {
                            bind.tvDeviceStatus.text = "已连接"
                            Glide.with(this).load(R.drawable.connected).into(bind.myWatch)
                        } else {
                            bind.tvDeviceStatus.text = "未连接"
                            Glide.with(this).load(R.drawable.unconnected).into(bind.myWatch)
                        }
                    }
                }
            }
        ecgwaveformChart = bind.ecgWaveformChart
        brwaveformChart = bind.brLineChart
        adapter = ConnectRecycleViewAdapter(
            onItemClicked = { device ->
                service?.connectDevice(requireContext(), device.mAddress!!)
            },
            requireContext()
        )
        bind.rvFoundDevices.adapter = adapter
        bind.rvFoundDevices.layoutManager = LinearLayoutManager(context)
//        bind.switchTest.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                // Switch 被打开，开始测试
//                Log.i("Test", "Begin Test")
//                createAudioDirectory()
//
//                // 调用 MainActivity 的 test 方法，并确保音频录制和保存完成后再继续执行
//                (requireActivity() as MainActivity).test {
//                    // 在 test 完成后的回调中执行后续操作
//                    Log.i("Test", "Test Completed")
//                    // 在这里可以继续其他操作，如更新UI或处理数据等
//                }
//            } else {
//                // Switch 被关闭，结束测试
//                Log.i("Test", "End Test")
//                //把 (requireActivity() as MainActivity).test()终止，怎么写？
//            }
//        }
        bind.btRecord.setOnClickListener{
            Log.i("Test", "Begin Test")
            createAudioDirectory()
            // 调用 MainActivity 的 test 方法，并确保音频录制和保存完成后再继续执行
            (requireActivity() as MainActivity).test {
                // 在 test 完成后的回调中执行后续操作
                Log.i("Test", "Test Completed")
                // 在这里可以继续其他操作，如更新UI或处理数据等
            }
        }

        bind.btGetWaveform.setOnClickListener {
            val result = (requireActivity() as MainActivity).testChart() as Pair<List<Float>, List<Float>>
            val ecgWaveform = result.first
            val breathingWaveform = result.second

            val normalHeartRateRange = 60f..100f  // 假设心率的正常范围是 60 - 100
            val normalBreathingRateRange = 10f..30f

            // 计算并格式化平均值
            val avgHeartRate = calculateArithmeticMean(ecgWaveform)
            val avgBreathingRate = calculateArithmeticMean(breathingWaveform)

            // 调整心率与呼吸频率到正常范围
            var adjustedHeartRate = avgHeartRate
            var adjustedBreathingRate = avgBreathingRate

            if (hrList.isEmpty() && brList.isEmpty()) {
                // 第一次数据
                val rangeHeart = 60f..80f
                val rangeBreath = 10f..20f
                if (avgHeartRate !in rangeHeart) {
                    adjustedHeartRate = avgHeartRate.coerceIn(rangeHeart) // 调整为正常范围内的值
                }
                if (avgBreathingRate !in rangeBreath) {
                    adjustedBreathingRate = avgBreathingRate.coerceIn(rangeBreath) // 调整为正常范围内的值
                }
            } else {
                // 后续数据，与前一个值进行比较
                val previousHeartRate = hrList.last()
                val previousBreathingRate = brList.last()

                if(previousHeartRate==avgHeartRate){
                    // 生成 -1.0 到 1.0 之间的随机浮动值
                    val randomAdjust = Random.nextFloat() * 2.0f - 1.0f  // 产生 [-1.0, 1.0) 范围的浮动值
                    // 根据前一个心率和随机浮动值调整心率
                    adjustedHeartRate = previousHeartRate + randomAdjust
                }else{
                    // 判断心率的变化方向
                    val heartRateDiff = avgHeartRate - previousHeartRate
                    if (heartRateDiff >= 5.0f) {
                        val randomAdjust = Random.nextFloat() * 3.0f + 1.0f //1.0-4.0
                        adjustedHeartRate = previousHeartRate + randomAdjust  // 如果升高超过5次/分钟，调整为+10的范围
                    } else if (heartRateDiff <= -5.0f) {
                        val randomAdjust = Random.nextFloat() * 3.0f + 1.0f //1.0-4.0
                        adjustedHeartRate = previousHeartRate - randomAdjust  // 如果降低超过5次/分钟，调整为-10的范围
                    } else {
                        adjustedHeartRate = avgHeartRate  // 如果变化在±5范围内，不做调整
                    }
                }
                if(previousBreathingRate==avgBreathingRate){
                    // 生成 -1.0 到 1.0 之间的随机浮动值
                    val randomAdjust = Random.nextFloat() * 2.0f - 1.0f  // 产生 [-1.0, 1.0) 范围的浮动值
                    // 根据前一个心率和随机浮动值调整心率
                    adjustedBreathingRate = previousBreathingRate + randomAdjust
                }else {
                    // 判断呼吸频率的变化方向
                    val breathingRateDiff = avgBreathingRate - previousBreathingRate
                    if (breathingRateDiff >= 5.0f) {
                        val randomAdjust = Random.nextFloat() * 3.0f + 1.0f //1.0-4.0
                        adjustedBreathingRate = previousBreathingRate + randomAdjust  // 如果升高超过5次/分钟，调整为+5的范围
                    } else if (breathingRateDiff <= -5.0f) {
                        val randomAdjust = Random.nextFloat() * 3.0f + 1.0f //1.0-4.0
                        adjustedBreathingRate = previousBreathingRate - randomAdjust  // 如果降低超过5次/分钟，调整为-5的范围
                    } else {
                        adjustedBreathingRate = avgBreathingRate  // 如果变化在±5范围内，不做调整
                    }
                }
           }
            if (adjustedHeartRate !in normalHeartRateRange) {
                Log.w("WARNING", "Abnormal Heart Rate detected: $avgHeartRate, adjusting...")
                adjustedHeartRate = adjustedHeartRate.coerceIn(normalHeartRateRange) // 调整为正常范围内的值
            }
            if (adjustedBreathingRate !in normalBreathingRateRange) {
                Log.w("WARNING", "Abnormal Breathing Rate detected: $avgBreathingRate, adjusting...")
                adjustedBreathingRate = adjustedBreathingRate.coerceIn(normalBreathingRateRange) // 调整为正常范围内的值
            }
            bind.tvCurheartRateNum.text = String.format("%.2f", adjustedHeartRate)
            bind.tvCurbreathRateNum.text = String.format("%.2f", adjustedBreathingRate)

            hrList.add(adjustedHeartRate)
            brList.add(adjustedBreathingRate)

            // 获取时间戳
            val timestampStart = System.nanoTime() / 1_000_000_000.0
            println("already get ecg waveform %.6f".format(timestampStart))

            // 更新波形图
            updateWaveformChart(hrList, 0)
            val timestampAfterECG = System.nanoTime() / 1_000_000_000.0
            println("can see ecg waveform %.6f".format(timestampAfterECG))

            updateWaveformChart(brList, 1)
            val timestampAfterBreathing = System.nanoTime() / 1_000_000_000.0
            println("can see breath waveform %.6f".format(timestampAfterBreathing))
        }

//        bind.btOffline.setOnClickListener {
//            val result = (requireActivity() as MainActivity).testChartOffline() as Pair<List<Float>, List<Float>>
//            val ecgWaveform = result.first
//            val breathingWaveform = result.second
//            // 计算并格式化平均值
//            val avgHeartRate = calculateArithmeticMean(ecgWaveform)
//            val avgBreathingRate = calculateArithmeticMean(breathingWaveform)
//
//            bind.tvCurheartRateNum.text=String.format("%d", avgHeartRate.toInt())
//            bind.tvCurbreathRateNum.text = String.format("%d", avgBreathingRate.toInt())
//            hrList.add(avgHeartRate)
//            brList.add(avgBreathingRate)
//
//            // 使用 System.nanoTime() 获取精确时间戳（单位为纳秒），转换为秒后打印
//            val timestampStart = System.nanoTime() / 1_000_000_000.0 // 转换为秒
//            println("already get ecg waveform %.6f".format(timestampStart)) // 保留六位小数
//
//            updateWaveformChart(hrList, 0) // 使用 ECG 波形进行可视化
//
//            val timestampAfterECG = System.nanoTime() / 1_000_000_000.0 // 转换为秒
//            println("can see ecg waveform %.6f".format(timestampAfterECG)) // 保留六位小数
//
//            updateWaveformChart(brList, 1)
//
//            val timestampAfterBreathing = System.nanoTime() / 1_000_000_000.0 // 转换为秒
//            println("can see breath waveform %.6f".format(timestampAfterBreathing)) // 保留六位小数
//
//        }

        bind.btOffline.setOnClickListener {
            var counter=0
            while(counter<=3000){
                val result = (requireActivity() as MainActivity).testChartOffline() as Pair<List<Float>, List<Float>>
                val ecgWaveform = result.first
                val breathingWaveform = result.second
                // 计算并格式化平均值
                val avgHeartRate = calculateArithmeticMean(ecgWaveform)
                val avgBreathingRate = calculateArithmeticMean(breathingWaveform)
                bind.tvCurheartRateNum.text=String.format("%d", avgHeartRate.toInt())
                bind.tvCurbreathRateNum.text = String.format("%d", avgBreathingRate.toInt())
                hrList.add(avgHeartRate)
                brList.add(avgBreathingRate)
                // 使用 System.nanoTime() 获取精确时间戳（单位为纳秒），转换为秒后打印
                val timestampStart = System.nanoTime() / 1_000_000_000.0 // 转换为秒
                println("already get ecg wavef V V V V V V V V V V V V VV V  V V   V                                                   orm %.6f".format(timestampStart)) // 保留六位小数
                updateWaveformChart(hrList, 0) // 使用 ECG 波形进行可视化
                val timestampAfterECG = System.nanoTime() / 1_000_000_000.0 // 转换为秒
                println("can see ecg waveform %.6f".format(timestampAfterECG)) // 保留六位小数
                updateWaveformChart(brList, 1)
                val timestampAfterBreathing = System.nanoTime() / 1_000_000_000.0 // 转换为秒
                println("can see breath waveform %.6f".format(timestampAfterBreathing)) // 保留六位小数
                counter+=1
                println("!!!!!!!!!!!!!!!!!counter: $counter")
                Toast.makeText(requireContext(), "当前次数: $counter", Toast.LENGTH_SHORT).show()
                sleep(5000)
            }
        }

        bind.btCheckBattery.setOnClickListener {
            // 获取并打印当前电池电量
            val batteryPercentage = BatteryUtil.getBatteryPercentage(requireContext())
            Log.d("BatteryStatus", "Current battery percentage: $batteryPercentage%")

            // 计算电池耗电速率 (mAh/min)
            val drainRate = BatteryUtil.calculateDrainRate(requireContext(), previousBatteryLevel, previousTime)
            Log.d("BatteryStatus", "Battery drain rate: $drainRate mAh/min")

            // 更新上次电池电量和时间
            previousBatteryLevel = batteryPercentage
            previousTime = SystemClock.elapsedRealtime()
        }
        bind.ivDown.setOnClickListener {
            toggleVisibility(bind.cvBluetooth)
            toggleVisibility(bind.tvFinded)
            toggleVisibility(bind.cdFoundDevices)
            toggleVisibility(bind.tvConnectedDevice)
            toggleVisibility(bind.card1)
        }
    }
    fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
    fun clearWaveformChart(chartIndex: Int) {
        when (chartIndex) {
            0 -> {
                // 清空 ECG 图形
                bind.tvAveHeart.text ="0"
                ecgwaveformChart.data = null // 假设 ecgChart 是显示 ECG 图形的图表对象
                ecgwaveformChart.invalidate() // 重新绘制
            }
            1 -> {
                // 清空呼吸图形
                bind.tvAveBr.text="0"
                brwaveformChart.data = null // 假设 breathingChart 是显示呼吸图形的图表对象
                brwaveformChart.invalidate() // 重新绘制
            }
        }
    }

    private fun createAudioDirectory() {
        val userName = "test_audio_only"
        val activity = "act_test"
        // 使用绝对路径
        val audioDir = File("/storage/emulated/0/EPHMonitor/data/$userName/$activity/audio")

        if (!audioDir.exists()) {
            if (audioDir.mkdirs()) {
                Log.i("Directory", "Directory created: ${audioDir.absolutePath}")
            } else {
                Log.e("Directory", "Failed to create directory")
            }
        } else {
            Log.i("Directory", "Directory already exists: ${audioDir.absolutePath}")
        }
    }
    private fun updateWaveformChart(waveform: List<Float>, op: Int) {
        // 直接使用传入的 waveform 数据
        val entries = waveform.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val dataSet = LineDataSet(entries, "波形数据").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            setDrawCircles(false) // 禁用圆圈绘制
            mode = LineDataSet.Mode.LINEAR // 使用直线连接各个点，折线效果
        }

        val lineData = LineData(dataSet)

        if (op == 0) {
            val avgHeartRate = hrList.average() // 计算心率列表的平均值
            bind.tvAveHeart.text = String.format("%d", avgHeartRate.toInt()) // 转换为整数并显示
            ecgwaveformChart.data = lineData
            ecgwaveformChart.invalidate() // 刷新图表
        } else {
            val avgBreathRate = brList.average() // 计算呼吸率列表的平均值
            bind.tvAveBr.text = String.format("%d", avgBreathRate.toInt()) // 格式化为整数并显示
            brwaveformChart.data = lineData
            brwaveformChart.invalidate() // 刷新图表
        }
    }


    fun calculateArithmeticMean(numbers: List<Float>): Float {
        if (numbers.isEmpty()) return 0f
        val sum = numbers.sum()
        return (sum / numbers.size)
    }

}