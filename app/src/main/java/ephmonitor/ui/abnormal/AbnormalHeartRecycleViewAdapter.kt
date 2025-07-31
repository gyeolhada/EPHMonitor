package com.example.ephmonitor.ui.abnormal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.BrCardItemBinding
import com.example.ephmonitor.databinding.EcgCardItemBinding
import com.example.ephmonitor.databinding.FragmentItemConnectBinding
import com.example.ephmonitor.model.BLEDeviceInfo
import com.example.ephmonitor.model.History
import com.example.ephmonitor.room.entity.Record
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet


class AbnormalHeartRecycleViewAdapter(private val items: List<Record>) : RecyclerView.Adapter<AbnormalHeartRecycleViewAdapter.ViewHolder>() {

    class ViewHolder(val binding: EcgCardItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EcgCardItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val maxHeartRate = item.heartRate.maxOrNull() ?: 0f
        val avgHeartRate = item.heartRate.average().toFloat()
        // 显示心率数据
        holder.binding.tvAbnormalHeartbeat.text = "${item.date} ${item.startTime}      ${String.format("%.2f", maxHeartRate)} bpm"
        holder.binding.tvAveHr.text = String.format("%.2f", avgHeartRate)

        // 获取LineChart组件
        val lineChart = holder.binding.hrLineChart

        // 创建心率数据点（模拟数据）
        val entries = item.heartRate.mapIndexed { index, value ->
            // 每个心率数据的index作为x值，心率值作为y值
            Entry(index.toFloat(), value)
        }

        // 创建数据集
        val lineDataSet = LineDataSet(entries, "Heart Rate")
        lineDataSet.color = ContextCompat.getColor(holder.itemView.context, R.color.warn_green) // 线条颜色
        lineDataSet.valueTextColor = ContextCompat.getColor(holder.itemView.context, R.color.black) // 数据点文字颜色
        lineDataSet.valueTextSize = 10f  // 设置文字大小
        lineDataSet.setDrawFilled(true)  // 设置填充色
        lineDataSet.fillColor = ContextCompat.getColor(holder.itemView.context, R.color.bg_main) // 填充色

        // 创建LineData对象并设置给LineChart
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData
        lineChart.invalidate() // 刷新图表

        // 配置图表（如果需要）
        lineChart.description.isEnabled = false // 关闭描述文字
        lineChart.legend.isEnabled = false // 隐藏图例
        lineChart.setDrawGridBackground(false) // 关闭背景网格
        lineChart.setTouchEnabled(false) // 关闭触摸事件
        lineChart.isDragEnabled = true // 启用拖动
        lineChart.isScaleXEnabled = true // 启用X轴缩放
        lineChart.isScaleYEnabled = false // 禁用Y轴缩放

    }

    override fun getItemCount(): Int = items.size
}