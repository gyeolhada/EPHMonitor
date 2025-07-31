package com.example.ephmonitor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.ephmonitor.MainActivity
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView

class HomeFragment : Fragment() {
    private lateinit var bind: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private var selectedCardType: MaterialCardView? = null
    private var selectedTextViewType: TextView? = null
    private var selectedCardTime: MaterialCardView? = null
    private var selectedTextViewTime: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentHomeBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.cvRun.setOnClickListener { toggleSelectionType(bind.cvRun, bind.tvRun) }
        bind.cvSwim.setOnClickListener { toggleSelectionType(bind.cvSwim, bind.tvSwim) }
        bind.cvWalk.setOnClickListener { toggleSelectionType(bind.cvWalk, bind.tvWalk) }
        bind.cvBike.setOnClickListener { toggleSelectionType(bind.cvBike, bind.tvBike) }
        bind.cvRow.setOnClickListener { toggleSelectionType(bind.cvRow, bind.tvRow) }
        bind.cvOther.setOnClickListener { toggleSelectionType(bind.cvOther, bind.tvOther) }
        bind.cv15m.setOnClickListener { toggleSelectionTime(bind.cv15m, bind.tv15m) }
        bind.cv30m.setOnClickListener { toggleSelectionTime(bind.cv30m, bind.tv30m) }
        bind.cv1h.setOnClickListener { toggleSelectionTime(bind.cv1h, bind.tv1h) }
        bind.cv15h.setOnClickListener { toggleSelectionTime(bind.cv15h, bind.tv15h) }


        bind.checkHeartRecord.setOnClickListener{
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_navigation_home_to_abnormalHeartFragment)
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility=View.GONE
        }
        bind.checkBreathRecord.setOnClickListener{
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_navigation_home_to_abnormalBreathFragment)
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility=View.GONE
        }

       bind.btNav.setOnClickListener{
           Navigation.findNavController(bind.root)
               .navigate(R.id.action_navigation_home_to_navFragment)
       }



        bind.cvSelfDefine.setOnClickListener {
            toggleSelectionTime(bind.cvSelfDefine, bind.tvSelfDefine)
            ExerciseTimeEditDialog(requireContext(), "") { content ->
                selectedTextViewTime?.text = content
            }.show()
        }
        bind.cvNoLimit.setOnClickListener { toggleSelectionTime(bind.cvNoLimit, bind.tvNoLimit) }
        bind.btAction.setOnClickListener{checkIfSelectionComplete()}
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
    }

    private fun toggleSelectionType(card: MaterialCardView, textView: TextView) {
        if (selectedCardType == card) {
            // 如果再次点击同一个卡片，则恢复原来的颜色
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            selectedCardType = null
            selectedTextViewType = null
        } else {
            // 如果点击不同的卡片，则更新选中的卡片，并设置新的颜色
            selectedCardType?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            selectedTextViewType?.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bt_main))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            selectedCardType = card
            selectedTextViewType = textView
        }
    }

    private fun toggleSelectionTime(card: MaterialCardView, textView: TextView) {
        if (selectedCardTime == card) {
            // 如果再次点击同一个卡片，则恢复原来的颜色
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            selectedCardTime = null
            selectedTextViewTime = null
        } else {
            // 如果点击不同的卡片，则更新选中的卡片，并设置新的颜色
            selectedCardTime?.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            selectedTextViewTime?.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            card.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bt_main))
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            selectedCardTime = card
            selectedTextViewTime = textView
        }
    }

    private fun checkIfSelectionComplete() {
        if (selectedCardType != null && selectedCardTime != null) {
            val selectedTypeText = selectedTextViewType?.text.toString()
            val selectedTimeText = selectedTextViewTime?.text.toString()
            Toast.makeText(requireContext(), "运动开始", Toast.LENGTH_SHORT).show()
            val bundle = Bundle().apply {
                putString("selectedType", selectedTypeText)
                putString("selectedTime", selectedTimeText)
            }
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_navigation_home_to_exerciseFragment, bundle)
        } else if(selectedCardType == null) {
            Toast.makeText(requireContext(), "请选择运动类型", Toast.LENGTH_SHORT).show()
        } else if(selectedCardType == null) {
            Toast.makeText(requireContext(), "请设置运动时长", Toast.LENGTH_SHORT).show()
        }
    }
}