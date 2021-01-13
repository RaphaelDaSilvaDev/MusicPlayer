package com.bits.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bits.musicplayer.fragments.ArtistFragment
import com.bits.musicplayer.fragments.SongList
import com.bits.musicplayer.fragments.SongsFragment
import com.bits.musicplayer.models.Song
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_slider.*


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

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 111)
        }else{
            startApp()
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            startApp()
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("CommitTransaction")
    private fun startApp(){
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.top_menu)

        mp = MediaPlayer()

        navigation()

        slidingPanel()

        listSong()

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
            Log.d("DATA", "TOUCH")
            when(it.itemId){
                R.id.songsFragment -> makeCurrentFragment(SongsFragment())
                R.id.artistFragment -> makeCurrentFragment(ArtistFragment())
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
    private fun listSong(){
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicFile= ArrayList<Song>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        musicFiles
        var id = -1
        val resolver = contentResolver
        val rs = resolver.query(uri, null, selection, null, null)
        if(rs != null){
            while (rs.moveToNext()){
                id ++
                val fullName = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                val title = fullName.replace(".mp3", "")
                val albumId = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val albumName = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artistId = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                val artistName = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val url = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DATA))
                val songDuration = rs.getString(rs.getColumnIndex(MediaStore.Audio.Media.DURATION))

                val listSongs = Song(id, title, albumId, albumName,artistId, artistName, url, songDuration)

                musicFile.add(listSongs)

                if(rs.isLast){
                    if(!musicFile.isNullOrEmpty()){
                        updateListSong(musicFile, id + 1)
                    }
                }
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
            mp.setDataSource(url)
            mp.prepare()
            updateSeekBar(mp.duration)
            updateInfo(fisrtSong)
        }else{
            musicFiles = arrayMusics
            totalSongs = totalMusics
            val fisrtSong = musicFiles!![0].id
            val url = musicFiles!![0].url
            mp.setDataSource(url)
            mp.prepare()
            updateSeekBar(mp.duration)
            updateInfo(fisrtSong)
        }
    }

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
        val totalTimeSong = musicFiles?.get(id)?.songDuration?.toInt()
        songName_small_songPlayer.isSelected = true
        songName_small_songPlayer.text = title.toString()
        songName_songPlayer.isSelected = true
        songName_songPlayer.text = title.toString()
        songAuthor_small_songPlayer.text = artistName.toString()
        songAuthor_songPlayer.text = artistName.toString()
        actualTime_songPlayer.text = "0:00"

        if (totalTimeSong != null) {
            val finalText = createTimeLabel(totalTimeSong)
            totalTime_songPlayer.text = finalText
        }

    }

    private fun playMusic(url: String, id: Int){

        updateInfo(id)

        if(mp.isPlaying){
            mp.stop()
            mp.reset()
            mp.setDataSource(url)
            mp.prepare()
            mp.start()
            isPlay = true
            playButton_small_songPlayer.isActivated = isPlay
            playButton_songPlayer.isActivated = isPlay
            actualSong = id
            updateSeekBar(mp.duration)
        }else{
            mp.reset()
            mp.setDataSource(url)
            mp.prepare()
            mp.start()
            isPlay = true
            playButton_small_songPlayer.isActivated = isPlay
            playButton_songPlayer.isActivated = isPlay
            actualSong = id
            updateSeekBar(mp.duration)
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