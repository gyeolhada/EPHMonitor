package com.example.ephmonitor.ui.mine

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentMineBinding
import com.example.ephmonitor.ui.login.LoginViewModel

class MineFragment : Fragment() {
    private lateinit var binding: FragmentMineBinding
    private lateinit var adapter: HistoryRecyclerViewAdapter
    private lateinit var viewModel: MineViewModel
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MineViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = loginViewModel.getCurrentUserId()
        viewModel.getHistoryItems(userId).observe(viewLifecycleOwner) { historyItems ->
            adapter = HistoryRecyclerViewAdapter(historyItems)
            binding.rvHistory.layoutManager = LinearLayoutManager(context)
            binding.rvHistory.adapter = adapter
        }
        binding.cvDetail.visibility = View.GONE
        binding.ivDown.setOnClickListener {
            if (binding.cvDetail.visibility == View.VISIBLE) {
                binding.cvDetail.visibility = View.GONE
            } else {
                binding.cvDetail.visibility = View.VISIBLE
            }
        }
        binding.btExit.setOnClickListener{
            loginViewModel.logout()
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_navigation_mine_to_loginFragment)
        }
        binding.tvAccount.text = loginViewModel.getCurrentUserAccount()
        Log.d("MineFragment", "User login in: ${loginViewModel.getCurrentUserAccount()}")
        binding.btEdit.setOnClickListener{
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_navigation_mine_to_mineEditFragment)
        }
        viewModel.getPerson(userId).observe(viewLifecycleOwner) { person ->
            person?.let {
                binding.tvName.text = it.pname
                binding.tvHeight.text = it.pheight
                binding.tvWeight.text = it.pweight
                binding.tvBMI.text = it.pBMI
                binding.tvAge.text = it.page
                loadAvatar(it.avatar)
            }
        }
    }
    private fun loadAvatar(uri: String?) {
        if (!uri.isNullOrEmpty()) {
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.brain)
                .into(binding.ivPhoto)
        }
    }
}
