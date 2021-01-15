package com.bits.musicplayer

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bits.musicplayer.fragments.ArtistFragment
import com.bits.musicplayer.fragments.FolderFragment
import com.bits.musicplayer.fragments.SongsFragment
import com.bits.musicplayer.models.Song
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_slider.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var musicFiles: ArrayList<Song>? = null
    lateinit var mp: MediaPlayer
    private var isPlay = false
    private var actualSong = 0
    private var totalSongs = 0
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startApp()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("CommitTransaction")
    private fun startApp(){
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.top_menu)

        mp = MediaPlayer()

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this))

        navigation()

        slidingPanel()

        playButton_small_songPlayer.setOnClickListener {
            playStopButton()
        }

        playButton_songPlayer.setOnClickListener {
            playStopButton()
        }

        nextButton_small_songPlayer.setOnClickListener {
            nextMusic()
        }

        nextButton_songPlayer.setOnClickListener {
            nextMusic()
        }

        previousButton_small_songPlayer.setOnClickListener {
            previousMusic()
        }

        previousButton_songPlayer.setOnClickListener {
            previousMusic()
        }
    }

    private fun navigation(){
        makeCurrentFragment(SongsFragment())
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.songsFragment -> makeCurrentFragment(SongsFragment())
                R.id.artistFragment -> makeCurrentFragment(ArtistFragment())
                R.id.folderFragment -> makeCurrentFragment(FolderFragment())
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) = supportFragmentManager.beginTransaction().apply {
        replace(R.id.fragment, fragment)
        commit()
    }

    private fun slidingPanel(){
        slider_panel.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener{
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                val text: ConstraintLayout = findViewById(R.id.small_songPlayer)
                text.alpha = 1 - slideOffset
                songPlayerFragment.alpha = 0 + slideOffset
                playButton_small_songPlayer.isClickable = text.alpha != 0f
                nextButton_small_songPlayer.isClickable = text.alpha != 0f
                previousButton_small_songPlayer.isClickable = text.alpha != 0f

            }

            override fun onPanelStateChanged(
                    panel: View?,
                    previousState: SlidingUpPanelLayout.PanelState?,
                    newState: SlidingUpPanelLayout.PanelState?
            ) {

            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("Recycle")
    fun listSong(arrayMusics: ArrayList<Song>, totalMusics: Int){
       if (musicFiles.isNullOrEmpty()){
           musicFiles = arrayMusics
           totalSongs = totalMusics
           try {
               mp.setOnPreparedListener(OnPreparedListener { mp -> updateInfo(musicFiles!![0].id) })
               mp.setDataSource(musicFiles!![0].url)
               mp.prepare()
               updateSeekBar(mp.duration)
           } catch (e: Exception) {
               e.printStackTrace()
           }
       }
    }

    fun updateListSong( arrayMusics: ArrayList<Song>, totalMusics: Int){
       if(!musicFiles.isNullOrEmpty()){
            mp.reset()
            musicFiles = arrayMusics
            totalSongs = totalMusics
            val fisrtSong = musicFiles!![0].id
            val url = musicFiles!![0].url

            try {
                mp.setOnPreparedListener(OnPreparedListener { mp -> mp.start() })
                mp.setDataSource(url)
                mp.prepare()
                updateSeekBar(mp.duration)
                updateInfo(fisrtSong)
            } catch (e: Exception) {
                e.printStackTrace()
            }
       }
    }

    @RequiresApi(Build.VERSION_CODES.O)
   fun songPlayMain(bundle: Bundle){
        val id = (bundle.get("id").toString()).toInt()
        val url = musicFiles?.get(id)?.url

        playMusic(url.toString(), id)
        updateSeekBar(mp.duration)
    }

    @SuppressLint("SetTextI18n")
    fun updateInfo(id: Int){
        val title = musicFiles?.get(id)?.title
        val artistName = musicFiles?.get(id)?.artistName
        songName_small_songPlayer.isSelected = true
        songName_small_songPlayer.text = title.toString()
        songName_songPlayer.isSelected = true
        songName_songPlayer.text = title.toString()
        songAuthor_small_songPlayer.text = artistName.toString()
        songAuthor_songPlayer.text = artistName.toString()
        actualTime_songPlayer.text = "0:00"

        val finalText = createTimeLabel(mp.duration)
        totalTime_songPlayer.text = finalText
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playMusic(url: String, id: Int){
        if(mp.isPlaying){
            mp.stop()
            mp.reset()
            try {
                mp.setOnPreparedListener(OnPreparedListener { mp -> mp.start() })
                mp.setDataSource(url)
                mp.prepare()
                isPlay = true
                playButton_small_songPlayer.isActivated = isPlay
                playButton_songPlayer.isActivated = isPlay
                actualSong = id
                updateSeekBar(mp.duration)
                updateInfo(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mp.reset()
            try {
                mp.setOnPreparedListener(OnPreparedListener { mp -> mp.start() })
                mp.setDataSource(url)
                mp.prepare()
                isPlay = true
                playButton_small_songPlayer.isActivated = isPlay
                playButton_songPlayer.isActivated = isPlay
                actualSong = id
                updateSeekBar(mp.duration)
                updateInfo(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playStopButton() {
        isPlay = if(isPlay){
            mp.pause()
            false

        }else{
            mp.start()
            true
        }
        playButton_small_songPlayer.isActivated = isPlay
        playButton_songPlayer.isActivated = isPlay
    }

    private fun updateSeekBar(duration: Int){
        //circleSeekBar.max = duration.toFloat()
        seekBar_songPlayer.max = duration
        seekBar_songPlayer.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener{
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                    ) {
                        if(fromUser){
                            mp.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {

                    }
                }
        )
        Thread(Runnable {
            while (true){
                try{
                    val msg = Message()
                    msg.what = mp.currentPosition
                    handler.sendMessage(msg)
                    Thread.sleep(1000)
                }catch (e: InterruptedException){

                }
            }
        }).start()
    }

    @SuppressLint("HandlerLeak")
    private var handler =  object : Handler(){
        @RequiresApi(Build.VERSION_CODES.O)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val currentPosition = msg.what
            val positionText = createTimeLabel(currentPosition)
            seekBar_songPlayer.progress = currentPosition
            //circleSeekBar.progress = currentPosition.toFloat()
            actualTime_songPlayer.text = positionText

            if(mp.currentPosition >= mp.duration){
                nextMusic()
            }
        }
    }

    private fun createTimeLabel(time: Int): String{
        var timeLabel = ""
        val min = time / 1000 / 60
        val sec = time / 1000 % 60

        timeLabel = "$min:"

        if(sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nextMusic() {
        var idSong = actualSong + 1

        if(idSong > totalSongs - 1){
            idSong = 0
        }

        val music = musicFiles?.get(idSong)
        val url = music?.url

        if (url != null) {
            playMusic(url, idSong)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun previousMusic() {
        var idSong = actualSong - 1

        if(idSong < 0){
            idSong = totalSongs -1
        }

        val music = musicFiles?.get(idSong)
        val url = music?.url

        if (url != null) {
            playMusic(url, idSong)
        }
    }
}