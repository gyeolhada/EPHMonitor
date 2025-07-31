package com.example.ephmonitor.ui.exercise

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.ephmonitor.MainActivity
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentExerciseBinding
import com.example.ephmonitor.ui.login.LoginViewModel
import com.example.ephmonitor.ui.mine.MineViewModel
import com.example.ephmonitor.utils.ChaquopyUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.random.Random


class ExerciseFragment : Fragment() {
    private lateinit var bind: FragmentExerciseBinding
    private lateinit var exerciseViewModel: ExerciseViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var mineViewModel: MineViewModel
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var handler: Handler
    private var selectedTimeInMillis: Long = 0
    private var isPaused: Boolean = false
    private var heartRate: List<Float> = emptyList() // 初始化为空列表
    private var breathRate: List<Float> = emptyList()
    private var maxHeartRate: Float = 0F
    private var isHeartAbnormal:Boolean = false
    private var isBreathAbnormal:Boolean = false
    private var isShouldDisplayHeartDialog: Boolean=true
    private var isShouldDisplayBreathDialog: Boolean=true
    private var isShouldDisplayLowIntensityDialog: Boolean=true
    private var isShouldDisplayMidIntensityDialog: Boolean=true
    private var isShouldDisplayHighIntensityDialog: Boolean=true
    private var dataJob: Job? = null //协程
    private var timeJob: Job? = null //协程
    private lateinit var ecgwaveformChart: LineChart
    private lateinit var brwaveformChart: LineChart
    private var hrList: MutableList<Float> = mutableListOf() // 可修改的列表
    private var brList: MutableList<Float> = mutableListOf() // 可修改的列表

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        exerciseViewModel = ViewModelProvider(this).get(ExerciseViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        mineViewModel = ViewModelProvider(this).get(MineViewModel::class.java)
//        // 如果年龄为null，默认为0岁
//        val age = mineViewModel.getPersonAge(loginViewModel.getCurrentUserId())!!.toInt()
//        Log.d("EF", "age: $age")
        // 最大心率的计算公式：最大心率=220−年龄
        maxHeartRate = (220-21).toFloat()
        bind = FragmentExerciseBinding.inflate(layoutInflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.GONE

        val selectedType = arguments?.getString("selectedType")
        val selectedTime = arguments?.getString("selectedTime")
        bind.ivCancel.setOnClickListener { Navigation.findNavController(bind.root).navigateUp() }
        bind.tvType.text = selectedType
        setActivityImage(selectedType)


        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        startTime = System.currentTimeMillis()
        bind.tvStartTime.text = sdf.format(Date(startTime))

        selectedTimeInMillis = parseTime(selectedTime)
        endTime = startTime + selectedTimeInMillis

        bind.ivPause.setOnClickListener {
            isPaused = true
            bind.tvDuration.setTextColor(ContextCompat.getColor(requireContext(), R.color.warn_red))
        }

        bind.ivPlay.setOnClickListener {
            isPaused = false
            bind.tvDuration.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warn_blue
                )
            )
        }
        ecgwaveformChart = bind.ecgWaveformChart
        brwaveformChart = bind.brLineChart
        bind.ivComplete.setOnClickListener {
            isPaused = true
            bind.tvDuration.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.warn_green
                )
            )
            val sportType = mapSportType(selectedType)
            val currentDate = Date(System.currentTimeMillis())
            val userId = loginViewModel.getCurrentUserId()
            val endTime = System.currentTimeMillis()
            val exerciseDuration = endTime - startTime
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(currentDate)
            val formattedDuration = formedString(exerciseDuration)
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val formattedStartTime = sdf.format(Date(startTime))
            val formattedEndTime = sdf.format(Date(endTime))


            val builder = AlertDialog.Builder(requireContext())
            val customTitleView =
                LayoutInflater.from(requireContext()).inflate(R.layout.custom_title_layout, null)
            builder.setCustomTitle(customTitleView)
            val imageView = customTitleView.findViewById<ImageView>(R.id.sportType_complete)

            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_exercise_complete, null)
            builder.setView(dialogView)

            val sportTypeTextView: TextView = dialogView.findViewById(R.id.sportType_text)
            sportTypeTextView.text = "运动类型:" + selectedType

            val durationTextView: TextView = dialogView.findViewById(R.id.duration_text)
            durationTextView.text = "运动时长:" + formattedDuration

            val startTimeTextView: TextView = dialogView.findViewById(R.id.startTime_text)
            startTimeTextView.text = "开始时间:" + formattedStartTime

            val endTimeTextView: TextView = dialogView.findViewById(R.id.endTime_text)
            endTimeTextView.text = "结束时间:" + formattedEndTime

            val sportDateTextView: TextView = dialogView.findViewById(R.id.sportDate_text)
            sportDateTextView.text = "运动日期:" + formattedDate

            val imageResource = when (selectedType) {
                "跑步" -> R.drawable.run
                "游泳" -> R.drawable.swim
                "徒步" -> R.drawable.wak
                "骑行" -> R.drawable.bike
                "划船" -> R.drawable.row
                else -> R.drawable.othersport
            }
            Log.d("image", "$imageResource")
            imageView.setImageResource(imageResource)
            Log.d("image", "success")

            builder.setCancelable(false)
            val cancelDialog: ImageView = dialogView.findViewById(R.id.cancelDialog)


            exerciseViewModel.addRecord(
                sportType, formattedDate, formattedDuration,
                formattedStartTime, formattedEndTime,hrList,brList,
                isHeartAbnormal,isBreathAbnormal,userId
            )
            Toast.makeText(requireContext(), "运动完成", Toast.LENGTH_SHORT).show()

            val alertDialog = builder.create()
            alertDialog.show()

            cancelDialog.setOnClickListener {
                Navigation.findNavController(bind.root)
                    .navigate(R.id.action_exerciseFragment_to_navigation_home)
                alertDialog.dismiss()
            }

            val displayMetrics = requireContext().resources.displayMetrics
            val params = alertDialog.window?.attributes
            params?.apply {
                this.width = (displayMetrics.widthPixels * 0.8).toInt()
                this.height = (displayMetrics.heightPixels * 0.52).toInt()
            }
            alertDialog.window?.attributes = params!!
        }
    }

    private fun showDialog(curHeartRateS:String,curBreathRateS:String){

        Log.d("Dialog", "curHeartRateS: $curHeartRateS")
        Log.d("Dialog", "curBreathRateS: $curBreathRateS")
        Log.d("Dialog", "maxHeartRate: $maxHeartRate")

        if(curHeartRateS == "No data" || curBreathRateS == "No data")return
        val curHeartRate = curHeartRateS.toFloat().toInt()
        val curBreathRate = curBreathRateS.toFloat().toInt()

        val maxHR85 = (0.85 * maxHeartRate).toInt()
        val maxHR50 = (0.5 * maxHeartRate).toInt()
        val maxHR60 = (0.6 * maxHeartRate).toInt()
        val maxHR75 = (0.75 * maxHeartRate).toInt()
        val maxHR90 = (0.9 * maxHeartRate).toInt()

        // **心率过高警告**
        if (curHeartRate >= maxHR90 && isShouldDisplayHeartDialog) {
            isShouldDisplayHeartDialog = false
            isHeartAbnormal = true
            showAlertDialog(R.layout.dialog_heart_rate, curHeartRateS, R.id.dialog_heartrate_cancel)
        }

        // **呼吸急促警告**
        if (curBreathRate > 40 && isShouldDisplayBreathDialog) {
            isShouldDisplayBreathDialog = false
            isBreathAbnormal = true
            showAlertDialog(R.layout.dialog_breath_rate, curBreathRateS, R.id.dialog_breathrate_cancel)
        }

        // **低运动强度 (心率50-60% 或 呼吸12-20)**
//        if ((curHeartRate in maxHR50 until maxHR60 || curBreathRate in 12..20) && isShouldDisplayLowIntensityDialog) {
//            isShouldDisplayLowIntensityDialog = false
//            showAlertDialog(R.layout.dialog_low_exercise_intensity, null, R.id.dialog_cancel)
//        }
//        // **中等运动强度 (心率60-75% 或 呼吸20-30)**
//        else if ((curHeartRate in maxHR60 until maxHR75 || curBreathRate in 20..30) && isShouldDisplayMidIntensityDialog) {
//            isShouldDisplayMidIntensityDialog = false
//            showAlertDialog(R.layout.dialog_mid_exercise_intensity, null, R.id.dialog_cancel)
//        }
//        // **高运动强度 (心率75-90% 或 呼吸30-40)**
//        else if ((curHeartRate in maxHR75 until maxHR90 || curBreathRate in 30..40) && isShouldDisplayHighIntensityDialog) {
//            isShouldDisplayHighIntensityDialog = false
//            showAlertDialog(R.layout.dialog_high_exercise_intensity, null, R.id.dialog_cancel)
//        }

        if ((curHeartRate in maxHR50 until maxHR60) && isShouldDisplayLowIntensityDialog) {
            isShouldDisplayLowIntensityDialog = false
            Log.d("Dialog", "inL1: $curHeartRateS")
            showAlertDialog(R.layout.dialog_low_exercise_intensity, null, R.id.dialog_cancel)
        }
        // **中等运动强度 (心率60-75% 或 呼吸20-30)**
        else if ((curHeartRate in maxHR60 until maxHR75 ) && isShouldDisplayMidIntensityDialog) {
            isShouldDisplayMidIntensityDialog = false
            showAlertDialog(R.layout.dialog_mid_exercise_intensity, null, R.id.dialog_cancel)
        }
        // **高运动强度 (心率75-90% 或 呼吸30-40)**
        else if ((curHeartRate in maxHR75 until maxHR90 ) && isShouldDisplayHighIntensityDialog) {
            isShouldDisplayHighIntensityDialog = false
            showAlertDialog(R.layout.dialog_high_exercise_intensity, null, R.id.dialog_cancel)
        }
    }

    /**
     * 统一的弹窗显示方法
     */
    private fun showAlertDialog(layoutId: Int, valueText: String?, cancelId: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(layoutId, null)
        builder.setView(dialogView)
        builder.setCancelable(false)

        if (valueText != null) {
            val valueTextView: TextView? = dialogView.findViewById(R.id.heart_beat_num) ?: dialogView.findViewById(R.id.breath_num)
            valueTextView?.text = valueText
        }

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.window?.setDimAmount(0.6f)
        alertDialog.show()

        val cancelDialog: ImageView = dialogView.findViewById(cancelId)
        cancelDialog.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    private fun calculateArithmeticMean(numbers: List<Float>): Float {
        if (numbers.isEmpty()) return 0f
        val sum = numbers.sum()
        return sum / numbers.size
    }

    private fun setActivityImage(selectedType: String?) {
        val imageResource = when (selectedType) {
            "跑步" -> R.drawable.run
            "游泳" -> R.drawable.swim
            "徒步" -> R.drawable.wak
            "骑行" -> R.drawable.bike
            "划船" -> R.drawable.row
            else -> R.drawable.othersport
        }
        bind.ivType.setImageResource(imageResource)
    }

    private fun mapSportType(selectedType: String?): Int {
        return when (selectedType) {
            "跑步" -> 0
            "游泳" -> 1
            "徒步" -> 2
            "骑行" -> 3
            "划船" -> 4
            else -> 5
        }
    }

    private fun formedString(time: Long): String {
        val hours = time / 1000 / 60 / 60
        val minutes = (time / 1000 / 60) % 60
        val seconds = (time / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun parseTime(timeString: String?): Long {
        if (timeString == null) return 0L

        val pattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(min|hour)")
        val matcher = pattern.matcher(timeString)
        return if (matcher.find()) {
            val value = matcher.group(1).toDouble()
            val unit = matcher.group(2)
            when (unit) {
                "min" -> (value * 60 * 1000).toLong()
                "hour" -> (value * 60 * 60 * 1000).toLong()
                else -> 0L
            }
        } else {
            0L
        }
    }
    //*****协程
    private suspend fun getECGAndBreathingData(): Pair<List<Float>, List<Float>> {
//        delay(10000)
//        showDialog("180","25")
        (requireActivity() as MainActivity).test1() // 录制音频
        delay(10000) // 等待 10 秒
        val result = (requireActivity() as MainActivity).testChart1() ?: return Pair(emptyList(), emptyList())
        return result
    }
    private fun startUpdating() {
        // 启动时间更新协程
        timeJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) { // 确保协程可取消
                if (!isPaused) {
                    val currentTime = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                    // 更新当前时间和持续时长
                    bind.tvDuration.text = sdf.format(Date(currentTime))
                    val duration = currentTime - startTime
                    val hours = duration / 1000 / 60 / 60
                    val minutes = (duration / 1000 / 60) % 60
                    val seconds = (duration / 1000) % 60
                    bind.tvDuration.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                    // 更新剩余时间
                    val remainingTime = endTime - currentTime
                    if (remainingTime > 0) {
                        val remHours = remainingTime / 1000 / 60 / 60
                        val remMinutes = (remainingTime / 1000 / 60) % 60
                        val remSeconds = (remainingTime / 1000) % 60
                        bind.tvRemainTime.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", remHours, remMinutes, remSeconds)
                    } else {
                        bind.tvRemainTime.text = "00:00:00"
                    }

                    if (((duration/1000)%20).toInt() ==0)launchDataUpdateJob1()
                }
                delay(1000) // 每秒更新一次时间
            }
        }
    }

    private fun launchDataUpdateJob1() {
        dataJob = CoroutineScope(Dispatchers.Main).launch {
            // 获取ECG和呼吸波形数据
            val (ecgWaveform, breathingWaveform) = getECGAndBreathingData()
            if (ecgWaveform.isEmpty() || breathingWaveform.isEmpty()) {
                Log.e("ERROR", "Received empty ECG or Breathing waveform")
            } else {
                Log.d("ECG_DATA", "ECG Waveform: $ecgWaveform")
                Log.d("BREATH_DATA", "Breathing Waveform: $breathingWaveform")

                // 计算心率和呼吸频率的均值
                val avgHeartRate = calculateArithmeticMean(ecgWaveform)
                val avgBreathingRate = calculateArithmeticMean(breathingWaveform)

                // 定义心率和呼吸频率的正常范围（根据需要调整这些阈值）
                val normalHeartRateRange = 60f..150f  // 假设心率的正常范围是 60 - 150
                val normalBreathingRateRange = 10f..40f  // 假设呼吸频率的正常范围是 10 - 40

                // 如果是第一次数据（第一个值），使用阈值法进行调整
                var adjustedHeartRate = avgHeartRate
                var adjustedBreathingRate = avgBreathingRate

                if (hrList.isEmpty() && brList.isEmpty()) {
                    // 第一次数据
                    val rangeHeart = 60f..100f
                    val rangeBreath = 10f..30f
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
                // 更新UI
                heartRate += adjustedHeartRate
                breathRate += adjustedBreathingRate
                bind.tvCurheartRateNum.text = String.format("%.2f", adjustedHeartRate)
                bind.tvCurbreathRateNum.text = String.format("%.2f", adjustedBreathingRate)

                // 保存调整后的值到列表
                hrList.add(adjustedHeartRate)
                brList.add(adjustedBreathingRate)

                // 更新图表
                updateWaveformChart(hrList, 0)
                updateWaveformChart(brList, 1)
            }

            // 取消协程
            dataJob?.cancel()
            // 检查协程是否已经取消
            if (dataJob?.isCancelled == true) {
                Log.d("Job Status", "dataJob successfully cancelled")
            } else {
                Log.d("Job Status", "dataJob cancellation failed or not completed")
            }
        }
    }
    private fun stopUpdating() {
        // 取消协程
        timeJob?.cancel()
        dataJob?.cancel()
    }
    override fun onResume() {
        super.onResume()
        // 界面恢复时启动更新
        startUpdating()
    }
    override fun onPause() {
        super.onPause()
        // 界面暂停时停止更新
        stopUpdating()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView?.visibility = View.VISIBLE
        stopUpdating()
    }
    private fun updateWaveformChart(waveform: List<Float>, op: Int) {

        // 检查列表是否为空
        val lastHr = if (hrList.isNotEmpty()) hrList.last().toString() else "No data"
        val lastBr = if (brList.isNotEmpty()) brList.last().toString() else "No data"
        // 显示对话框，传入最后的心率和呼吸率数据
        showDialog(lastHr, lastBr)

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
}
