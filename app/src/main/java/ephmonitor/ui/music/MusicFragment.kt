package com.example.ephmonitor.ui.music

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentMusicBinding
import com.example.ephmonitor.model.Song
import com.example.ephmonitor.service.MusicService


class MusicFragment : Fragment() {

    private var musicService: MusicService? = null
    private var isServiceBound = false
    private lateinit var songList: List<Song>
    private lateinit var binding: FragmentMusicBinding
    private lateinit var adapter: MusicRecycleViewAdapter
    private lateinit var rotateAnimator: ObjectAnimator

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicBinder
            musicService = binder?.getService()
            isServiceBound = true

            musicService?.setSongList(songList)
            Log.d("MusicService", "songList: ${songList.size}")

            updateSongTitle()
            updatePlayPauseIcon()
            updateProgressBar()

            musicService?.mediaPlayer?.setOnPreparedListener {
                binding.songProgress.max = musicService!!.mediaPlayer.duration
                updateProgressBar()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isServiceBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 在这里获取歌曲列表
        songList = getSongList()
        Log.d("MusicFragment", "songList: ${songList.size}")

        // 启动服务并绑定
        val intent = Intent(requireContext(), MusicService::class.java)
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // 初始化 ViewBinding
        binding = FragmentMusicBinding.inflate(inflater, container, false)

        // 创建旋转动画
        rotateAnimator = ObjectAnimator.ofFloat(binding.albumArt, "rotation", 0f, 360f).apply {
            duration = 8000 // 8秒完成一圈
            interpolator = LinearInterpolator() // 匀速旋转
            repeatCount = ValueAnimator.INFINITE // 无限循环
        }

        // 初始化 RecyclerView 和适配器
        adapter = MusicRecycleViewAdapter(songList) { song -> adapter.notifyDataSetChanged() }
        binding.playlistRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.playlistRecyclerView.adapter = adapter

        // 处理播放/暂停按钮点击事件
        binding.playPauseIv.setOnClickListener {
            musicService?.pauseOrResumeMusic()
            updatePlayPauseIcon()
            updateProgressBar()
        }

        // 处理进度条变化
        binding.songProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 更新进度条
        musicService?.mediaPlayer?.setOnPreparedListener {
            binding.songProgress.max = musicService!!.mediaPlayer.duration
            updateProgressBar()
        }

        binding.togglePlaylistIv.setOnClickListener {
            toggleVisibility(binding.playlistRecyclerView)
        }

        binding.listIv.setOnClickListener {
            musicService?.changePlayMode()?.let { mode -> updatePlayModeIcon(mode) }
        }

        binding.nextIv.setOnClickListener {
            musicService?.playNextSong()
            updateSongTitle()
            updatePlayPauseIcon()
        }

        binding.prevIv.setOnClickListener {
            musicService?.playPreviousSong()
            updateSongTitle()
            updatePlayPauseIcon()
        }

        binding.ivBack.setOnClickListener {
            if (musicService?.isPlaying() == true) {
                AlertDialog.Builder(requireContext())
                    .setTitle("音乐正在播放")
                    .setMessage("是否继续播放？")
                    .setPositiveButton("继续播放") { _, _ -> }
                    .setNegativeButton("暂停") { _, _ -> musicService?.pauseOrResumeMusic() }
                    .setNeutralButton("查看播放器") { _, _ -> /* 跳转到播放器页面 */ }
                    .show()
            }
            Navigation.findNavController(binding.root).navigateUp()
        }
        return binding.root // 确保返回的是 binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isServiceBound) {
            requireActivity().unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun getSongList(): List<Song> {
        return listOf(
            Song("所念皆星河 -昼夜", R.raw.star_song),
            Song("Show You Can -XG", R.raw.show_you_can),
            Song("溯 -柳轻颂", R.raw.piano_music),
            Song("Bubble Gum -NJZ", R.raw.bubblegum),
            Song("我们的明天 -昼夜", R.raw.sample_music),
            Song("Shooting Star -XG", R.raw.shooting_star),
        )
    }

    private fun updateSongTitle() {
        val currentSongIndex = musicService?.getCurrentSong() ?: 0
        binding.songTitle.text = songList[currentSongIndex].name
    }

    private fun toggleVisibility(view: View) {
        view.visibility = if (view.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun updatePlayModeIcon(mode: String) {
        when (mode) {
            "SEQUENTIAL" -> {
                // 顺序播放
                binding.listIv.setImageResource(R.drawable.ic_list) // 设置顺序播放图标
            }
            "RANDOM" -> {
                // 单曲循环
                binding.listIv.setImageResource(R.drawable.ic_shuffle) // 设置单曲循环图标
            }
            "REPEAT_ONE" -> {
                // 随机播放
                binding.listIv.setImageResource(R.drawable.ic_repeat) // 设置随机播放图标
            }
        }
    }

    private fun updateProgressBar() {
        val mediaPlayer = musicService?.mediaPlayer
        if (mediaPlayer != null && mediaPlayer.isPlaying) {
            if (binding.songProgress.max != mediaPlayer.duration) {
                binding.songProgress.max = mediaPlayer.duration
            }
            binding.songProgress.progress = mediaPlayer.currentPosition

            val minutes = (mediaPlayer.currentPosition / 1000) / 60
            val seconds = (mediaPlayer.currentPosition / 1000) % 60
            binding.songDuration.text = String.format("%02d:%02d", minutes, seconds)
        }
        binding.songProgress.postDelayed({
            updateProgressBar()
        }, 1000)
    }

    private fun updatePlayPauseIcon() {
        if (musicService?.isPlaying() == true) {
            binding.playPauseIv.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseIv.setImageResource(R.drawable.ic_play)
        }
    }
}
