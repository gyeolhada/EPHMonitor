package com.example.ephmonitor.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.ephmonitor.R
import com.example.ephmonitor.room.entity.Record
import com.example.ephmonitor.databinding.FragmentItemSportBinding
import com.example.phychosiolz.data.enums.SportType
import java.util.Locale

class SportRecyclerViewAdapter(private val items: List<Record>) : RecyclerView.Adapter<SportRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(val binding: FragmentItemSportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentItemSportBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvDuration.text = item.duration
        val sportType = SportType.fromCode(item.sportType)
        holder.binding.tvType.text = sportType.description
        holder.binding.ivType.setImageResource(getImageResource( sportType.description))
        holder.binding.ivDetail.setOnClickListener{
            val bundle = Bundle().apply {
                putParcelable("record", item)
            }
            Navigation.findNavController(holder.binding.root)
                .navigate(R.id.action_navigation_mine_to_historyFragment,bundle)
        }
    }

    override fun getItemCount(): Int = items.size

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
