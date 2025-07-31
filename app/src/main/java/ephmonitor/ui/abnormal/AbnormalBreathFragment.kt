package com.example.ephmonitor.ui.abnormal

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ephmonitor.MainActivity
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentConnectBinding
import com.example.ephmonitor.enums.DeviceStatus
import com.example.ephmonitor.service.UserService
import java.text.SimpleDateFormat
import java.util.Date
import com.bumptech.glide.Glide
import com.example.ephmonitor.databinding.FragmentAbnormalBreathBinding
import com.example.ephmonitor.databinding.FragmentExerciseBinding
import com.example.ephmonitor.ui.connect.ConnectRecycleViewAdapter
import com.example.ephmonitor.ui.exercise.ExerciseViewModel
import com.example.ephmonitor.ui.login.LoginViewModel
import com.example.ephmonitor.ui.mine.HistoryRecyclerViewAdapter
import com.example.ephmonitor.utils.BatteryUtil
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.util.Locale

class AbnormalBreathFragment : Fragment() {
    private lateinit var bind: FragmentAbnormalBreathBinding
    private lateinit var abnormalViewModel: AbnormalViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var adapter: AbnormalBreathRecycleViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentAbnormalBreathBinding.inflate(layoutInflater)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        abnormalViewModel = ViewModelProvider(this).get(AbnormalViewModel::class.java)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val uid = loginViewModel.getCurrentUserId()
        abnormalViewModel.getAbnormalBR(uid).observe(viewLifecycleOwner) { items ->
            adapter = AbnormalBreathRecycleViewAdapter(items)
            bind.rvAbnormalBreath.layoutManager = LinearLayoutManager(context)
            bind.rvAbnormalBreath.adapter = adapter
        }

        bind.ivBack.setOnClickListener{
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_abnormalBreathFragment_to_navigation_home)
        }
    }
}