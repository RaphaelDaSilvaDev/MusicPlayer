package com.bits.musicplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import com.bits.musicplayer.fragments.ArtistFragment
import com.bits.musicplayer.fragments.FolderFragment
import com.bits.musicplayer.fragments.SongsFragment
import com.bits.musicplayer.models.Song
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_slider.*


class MainActivity : AppCompatActivity() {
    private var musicFiles: ArrayList<Song>? = null
    lateinit var mp: MediaPlayer
    private var isPlay = false
    private var actualSong = 0
    private var totalSongs = 0

    var ACTION_PREVIOUS: String = "actionprevious"
    var ACTION_NEXT: String = "actionnext"
    var ACTION_PLAY: String = "actionplay"

    var actionName: String? = null

    private var mediaSessionCompat: MediaSessionCompat? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startApp()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("CommitTransaction")
    private fun startApp() {
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.top_menu)

        mediaSessionCompat = MediaSessionCompat(baseContext, "My Audio")

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

        notificationController()
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notification(id: Int){

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val prevIntent = Intent(this, NotificationReceiver::class.java)
        prevIntent.action = ACTION_PREVIOUS
        val prevPending: PendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(this, NotificationReceiver::class.java)
        pauseIntent.action = ACTION_PLAY
        val pausePending: PendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(this, NotificationReceiver::class.java)
        nextIntent.action = ACTION_NEXT
        val nextPending: PendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, 1.toString())
            .setSmallIcon(R.drawable.ic_logo_black)
            .setColor(Color.rgb(32,32,32))
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_song))
            .setContentTitle(musicFiles?.get(id)?.title)
            .setContentText(musicFiles?.get(id)?.artistName)
            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previus", prevPending)
            .addAction(R.drawable.play_stop_24, "Pause", pausePending)
            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", nextPending)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat?.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)


        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

    private fun notificationController(){
        val action = registerReceiver(broadcastReceiver, IntentFilter("ActionName"))
        Log.d("DATA", "Action: $action")

        if(actionName != null){
            when(actionName){
                "playPause" -> Toast.makeText(this, "Play Pause", Toast.LENGTH_SHORT).show()
                "next" -> Toast.makeText(this, "Next", Toast.LENGTH_SHORT).show()
                "previous" -> Toast.makeText(this, "Previous", Toast.LENGTH_SHORT).show()
            }
        }

    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
           Log.d("DATA", "BroadcastReciver")
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateListSong(arrayMusics: ArrayList<Song>, totalMusics: Int){
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

        notification(id)
        playMusic(url.toString(), id)
        updateSeekBar(mp.duration)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        notification(id)

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun playStopButton() {
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
     fun nextMusic() {
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
    fun previousMusic() {
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