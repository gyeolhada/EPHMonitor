package com.example.ephmonitor.ui.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ephmonitor.R
import com.example.ephmonitor.model.Song

class MusicRecycleViewAdapter(private val songList: List<Song>, private val onDeleteClick: (Song) -> Unit) :
    RecyclerView.Adapter<MusicRecycleViewAdapter.SongViewHolder>() {

    // Step 2: Create the ViewHolder class
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songNameTextView: TextView = itemView.findViewById(R.id.tv_song_name)
        val deleteImageView: ImageView = itemView.findViewById(R.id.iv_delete)

        init {
            // Handle delete button click
            deleteImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Trigger the callback function when delete is clicked
                    onDeleteClick(songList[position])
                }
            }
        }
    }

    // Step 3: Create the onCreateViewHolder function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_music, parent, false)
        return SongViewHolder(itemView)
    }

    // Step 4: Create the onBindViewHolder function
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentSong = songList[position]
        holder.songNameTextView.text = currentSong.name
    }

    // Step 5: Return the size of the dataset
    override fun getItemCount(): Int {
        return songList.size
    }
}
