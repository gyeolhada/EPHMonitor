package com.example.ephmonitor.ui.mine

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ephmonitor.databinding.FragmentItemHistoryBinding
import com.example.ephmonitor.model.History
import com.example.ephmonitor.ui.history.SportRecyclerViewAdapter

class HistoryRecyclerViewAdapter(private val items: List<History>) : RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(val binding: FragmentItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentItemHistoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvDate.text = item.date
        holder.binding.rvSport.adapter = SportRecyclerViewAdapter(item.sports)
    }

    override fun getItemCount(): Int = items.size
}
