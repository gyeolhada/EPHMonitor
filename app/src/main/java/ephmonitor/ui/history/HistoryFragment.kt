package com.example.ephmonitor.ui.history

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.ephmonitor.MainActivity
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentHistoryBinding
import com.example.ephmonitor.room.entity.Person
import com.example.ephmonitor.room.entity.Record
import com.example.ephmonitor.ui.login.LoginViewModel
import com.example.ephmonitor.ui.mine.MineViewModel
import com.example.phychosiolz.data.enums.SportType
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HistoryFragment : Fragment() {
    private lateinit var bind: FragmentHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var mineViewModel: MineViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var record: Record
    private lateinit var ecgwaveformChart: LineChart
    private lateinit var brwaveformChart: LineChart
    private lateinit var caloriesChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHistoryBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        arguments?.let {
            record = it.getParcelable("record")!!
        }
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.ivBack.setOnClickListener {
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_historyFragment_to_navigation_mine)
        }

        ecgwaveformChart = bind.heartbeatLineHart
        brwaveformChart = bind.BRLineChart
        caloriesChart = bind.caloriesLineHart // 初始化卡路里图表

        val sportType = SportType.fromCode(record.sportType)
        bind.tvType.text = sportType.description
        bind.ivType.setImageResource(getImageResource(sportType.description))
        bind.tvSportDate.text = record.date
        bind.tvDuration.text = record.duration
        bind.tvStartTime.text = record.startTime
        bind.tvEndTime.text = record.endTime

        // 从 record 中获取心率和呼吸速率数据
        val ecgWaveform = record.heartRate // 假设 heartRate 是 List<Float>
        val breathingWaveform = record.breathRate // 假设 breathRate 是 List<Float>

        // 计算并格式化平均值
        val avgHeartRate = calculateArithmeticMean(ecgWaveform)
        val avgBreathingRate = calculateArithmeticMean(breathingWaveform)
        bind.tvAveHeart.text = String.format("%.2f", avgHeartRate) // 保留两位小数
        bind.tvAveBR.text = String.format("%.2f", avgBreathingRate) // 保留两位小数

        // 使用 System.nanoTime() 获取精确时间戳（单位为纳秒），转换为秒后打印
        val timestampStart = System.nanoTime() / 1_000_000_000.0 // 转换为秒
        println("already get ecg waveform %.6f".format(timestampStart)) // 保留六位小数

        updateWaveformChart(ecgWaveform, 0) // 使用 ECG 波形进行可视化

        val timestampAfterECG = System.nanoTime() / 1_000_000_000.0 // 转换为秒
        println("can see ecg waveform %.6f".format(timestampAfterECG)) // 保留六位小数

        updateWaveformChart(breathingWaveform, 1) // 使用呼吸波形进行可视化

        val timestampAfterBreathing = System.nanoTime() / 1_000_000_000.0 // 转换为秒
        println("can see breath waveform %.6f".format(timestampAfterBreathing)) // 保留六位小数

//        val weight = mineViewModel.getPersonWeight(loginViewModel.getCurrentUserId())
//        Log.d("HF", "weight: $weight")

        // 计算累计卡路里消耗
        val cumulativeCaloriesData = calculateCumulativeCalories(ecgWaveform, breathingWaveform,record.sportType,70)

        // 显示总卡路里消耗
        val totalCalories = cumulativeCaloriesData.lastOrNull() ?: 0f // 获取最后一个累计值
        bind.tvAveCalories.text = String.format("%.2f", totalCalories) // 显示消耗的卡路里

        // 绘制卡路里图
        setupCaloriesChart(caloriesChart, cumulativeCaloriesData) // 使用 List<Float> 绘制累计卡路里消耗


        (requireActivity() as MainActivity).requestFitnessAdvice(
            record.heartRate,
            record.breathRate,
            totalCalories,
            SportType.fromCode(record.sportType).description
        ) { advice ->
            // 在主线程更新 UI
            activity?.runOnUiThread {
                if (advice.startsWith("Error:")) {
                    bind.tvSportAdviceContent.text = "Failed: $advice"  // 显示错误
                } else {
                    bind.tvSportAdviceContent.text = advice  // 显示正常结果
                }
            }
        }
    }

    private fun updateWaveformChart(waveform: List<Float>, op: Int) {
        val smoothedWaveform = smoothWaveform(waveform)
        val entries = smoothedWaveform.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val dataSet = LineDataSet(entries, "波形数据").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            setDrawCircles(false) // 禁用圆圈绘制
            mode = LineDataSet.Mode.CUBIC_BEZIER // 使用三次贝塞尔曲线
        }

        val lineData = LineData(dataSet)

        if (op == 0) {
            ecgwaveformChart.data = lineData
            ecgwaveformChart.invalidate() // 刷新图表
        } else {
            brwaveformChart.data = lineData
            brwaveformChart.invalidate() // 刷新图表
        }
    }

    private fun smoothWaveform(waveform: List<Float>): List<Float> {
        // 实现简单的平滑方法（例如移动平均）
        val smoothed = mutableListOf<Float>()
        for (i in waveform.indices) {
            val left = if (i > 0) waveform[i - 1] else waveform[i]
            val right = if (i < waveform.size - 1) waveform[i + 1] else waveform[i]
            smoothed.add((left + waveform[i] + right) / 3)
        }
        return smoothed
    }

    fun calculateArithmeticMean(numbers: List<Float>): Float {
        if (numbers.isEmpty()) return 0f
        val sum = numbers.sum()
        return sum / numbers.size
    }


    // 计算累计卡路里消耗的方法
    fun calculateCumulativeCalories(
        heartRates: List<Float>,
        breathingRates: List<Float>,
        exerciseType: Int,      // 运动类型
        weight: Int          // 用户体重（kg）
    ): List<Float> {
        val cumulativeCaloriesList = mutableListOf<Float>()
        val dataSize = Math.min(heartRates.size, breathingRates.size) // 确保数据大小一致
        val secondsPerRecord = 2 // 每2秒记录一次

        val exercise = ExerciseType.values()[exerciseType]  // 获取对应运动类型的 MET、心率系数和呼吸频率系数
        val met = exercise.met
        val heartRateCoefficient = exercise.heartRateCoefficient
        val breathingRateCoefficient = exercise.breathingRateCoefficient

        var cumulativeCalories = 0f // 初始化累计卡路里

        for (i in 0 until dataSize) {
            // 计算当前记录的持续时间（以小时为单位）
            val durationInHours = secondsPerRecord / 3600.0 // 每条记录的持续时间

            // 计算心率卡路里消耗，基于运动类型的系数
            val heartRateCalories = heartRates[i] * heartRateCoefficient
            val breathingRateCalories = breathingRates[i] * breathingRateCoefficient

            // 基于心率、呼吸率、MET、体重和时间计算总卡路里消耗
            val totalCalories = (heartRateCalories + breathingRateCalories) * durationInHours * met * weight

            cumulativeCalories += totalCalories.toFloat() // 更新累计卡路里
            cumulativeCaloriesList.add(cumulativeCalories) // 将累计值添加到列表中
        }

        return cumulativeCaloriesList
    }


    private fun setupCaloriesChart(caloriesChart: LineChart, caloriesData: List<Float>) {
        val calorieEntries = caloriesData.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val calorieDataSet = LineDataSet(calorieEntries, "Calories Burned").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 10f
            setDrawCircles(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(calorieDataSet)
        caloriesChart.data = lineData
        caloriesChart.axisLeft.axisMinimum = 0f
        caloriesChart.axisLeft.axisMaximum = caloriesData.maxOrNull() ?: 100f
        caloriesChart.invalidate()
    }

    private fun getImageResource(type: String): Int {
        return when (type) {
            "跑步" -> R.drawable.run
            "游泳" -> R.drawable.swim
            "徒步" -> R.drawable.wak
            "骑行" -> R.drawable.bike
            "划船" -> R.drawable.row
            else -> R.drawable.othersport
        }
    }
}
// 为不同运动类型设定不同的心率和呼吸率系数
enum class ExerciseType(val met: Float, val heartRateCoefficient: Float, val breathingRateCoefficient: Float) {
    RUNNING(9.8f, 0.8f, 0.3f), // 跑步：MET值高，心率和呼吸频率系数较高
    SWIMMING(7.0f, 0.6f, 0.25f), // 游泳：MET值较高，心率和呼吸频率系数适中
    WALKING(3.8f, 0.5f, 0.2f), // 徒步：MET值低，心率和呼吸频率系数较低
    CYCLING(6.0f, 0.7f, 0.28f), // 骑行：MET值较高，心率和呼吸频率系数较高
    ROWING(7.5f, 0.65f, 0.27f), // 划船：MET值较高，心率和呼吸频率系数适中
    REST(1.0f, 0.3f, 0.15f) // 静息：MET值非常低，心率和呼吸频率系数较低
}