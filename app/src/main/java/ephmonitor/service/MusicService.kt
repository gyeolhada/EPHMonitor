package com.example.ephmonitor.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.ephmonitor.enums.PlayMode
import com.example.ephmonitor.model.Song

class MusicService : Service() {

    private val binder = MusicBinder()
    lateinit var mediaPlayer: MediaPlayer
    private var songList: List<Song> = emptyList()
    private var currentSongIndex = 0
    private var playMode = PlayMode.SEQUENTIAL  // 播放模式

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun setSongList(songs: List<Song>) {
        if (songList.isEmpty()) {
            songList = songs
            Log.d("MusicService", "songList: ${songList.size}")
            mediaPlayer = MediaPlayer.create(this, songList[0].resourceId).apply {
                setOnCompletionListener { playNextSong() }
                start()
            }
            pauseOrResumeMusic()
        }
    }

    fun playMusic(index: Int) {
        if (index < 0 || index >= songList.size) return

        mediaPlayer?.apply {
            if (isPlaying) pause()
            release()
        }

        currentSongIndex = index
        mediaPlayer = MediaPlayer.create(this, songList[index].resourceId).apply {
            setOnCompletionListener { playNextSong() }
            start()
        }
    }

    fun pauseOrResumeMusic() {
        mediaPlayer.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.start()
            }
        }
    }

    fun playNextSong() {
        currentSongIndex = when (playMode) {
            PlayMode.SEQUENTIAL -> (currentSongIndex + 1) % songList.size
            PlayMode.RANDOM -> (songList.indices).random()
            PlayMode.REPEAT_ONE -> currentSongIndex
        }
        playMusic(currentSongIndex)
    }

    fun playPreviousSong() {
        currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else songList.size - 1
        playMusic(currentSongIndex)
    }

    fun changePlayMode() :String{
        playMode = when (playMode) {
            PlayMode.SEQUENTIAL -> PlayMode.RANDOM
            PlayMode.RANDOM -> PlayMode.REPEAT_ONE
            PlayMode.REPEAT_ONE -> PlayMode.SEQUENTIAL
        }
        return playMode.toString()
    }

    fun getCurrentSong():Int{
        return currentSongIndex
    }

    fun isPlaying():Boolean{
        return mediaPlayer.isPlaying
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.apply {
            if (isPlaying) pause()
            release()
        }
    }
}
