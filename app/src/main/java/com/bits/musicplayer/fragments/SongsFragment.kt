package com.bits.musicplayer.fragments

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bits.musicplayer.MainActivity
import com.bits.musicplayer.R
import com.bits.musicplayer.models.Song
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.android.synthetic.main.song_list.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SongsFragment : Fragment() {
    companion object{
        var SONG_INFO = "SONG_INFO"
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        lifecycleScope.launch {
            delay(150L)
            loadSong()
            closeAllOpenFragments()
        }
        return  view
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun loadSong(){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val musicFiles= ArrayList<Song>()
        var adapterMax = 0
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
        var id = -1
        val resolver = requireActivity().contentResolver
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
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context))
                adapter.add(SongList(listSongs))
                musicFiles.add(listSongs)
            }
            val rsMax = rs.count
            adapterMax = adapter.itemCount
            if(adapterMax >= rsMax){
                adapter.notifyDataSetChanged()

                loadListSongs(adapter)
            }
        }

        adapter.setOnItemClickListener { item, view ->
            val songInfo = item as SongList
            val bundle = Bundle()
            bundle.putInt("id", songInfo.songList.id)
            (activity as MainActivity).updateListSong(musicFiles , adapterMax)
            (activity as MainActivity).songPlayMain(bundle)
        }
    }

    private fun loadListSongs(adapter: GroupAdapter<GroupieViewHolder>){
        listSongView.isVisible = true
        loading_songs.isVisible = false
        listSongView.adapter = adapter
    }

    private fun closeAllOpenFragments(){
        val openArtistFragment = activity?.supportFragmentManager?.findFragmentByTag("openArtistFragment")
        if (openArtistFragment != null) {
            activity?.supportFragmentManager?.beginTransaction()?.remove(openArtistFragment)?.commit()
        }
    }

}

class SongList(val songList: Song): com.xwray.groupie.Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.song_list
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.songName.text = songList.title
        viewHolder.itemView.songArtist.text = songList.artistName
        viewHolder.itemView.songName.isSelected = true

        ImageLoader.getInstance().displayImage(getImage(songList.albumId.toLong()).toString(),
                viewHolder.itemView.songImage, DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.ic_song)
                .resetViewBeforeLoading(true)
                .build())
    }

    //Get Album Image
    private fun getImage(albumId: Long): Any {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }

}
