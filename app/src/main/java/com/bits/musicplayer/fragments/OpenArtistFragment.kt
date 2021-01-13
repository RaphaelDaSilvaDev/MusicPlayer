package com.bits.musicplayer.fragments

import android.annotation.SuppressLint
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bits.musicplayer.MainActivity
import com.bits.musicplayer.R
import com.bits.musicplayer.models.Album
import com.bits.musicplayer.models.Song
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.album_list.view.*
import kotlinx.android.synthetic.main.fragment_open_artist.*
import kotlinx.android.synthetic.main.song_list.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OpenArtistFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_open_artist, container, false)
        lifecycleScope.launch {
            delay(150L)
            start()
        }
        return view
    }

    private fun start(){
        val bundle = this.arguments
        if(bundle != null){
            updateArtistInfo(bundle)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun updateArtistInfo(bundle: Bundle){
        val nameArtist = bundle.getString("name")
        val albumCount = bundle.getString("albumCount")
        val songCount = bundle.getString("songCount")
        val artistCover = bundle.getString("artistCover")

        artistname_artist_open.text = nameArtist
        albuns_artist_open.text = "Albums: $albumCount"
        songs_artist_open.text = "Songs: $songCount"

        if (artistCover != null) {
            ImageLoader.getInstance().displayImage(getAlbumImage(artistCover.toLong()).toString(),
                    album_image_artist_open, DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .showImageOnLoading(R.drawable.ic_artist)
                    .resetViewBeforeLoading(true)
                    .build())
        }

        if (nameArtist != null) {
            updateAlbums(nameArtist)
            updateSongs(nameArtist)
        }

    }

    @SuppressLint("Recycle")
    private fun updateAlbums(nameArtist: String){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val resolver = requireActivity().contentResolver
        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val where = MediaStore.Audio.Albums.ARTIST + " LIKE ?"
        val like: String? = nameArtist
        val album = resolver.query(uri, null, where, arrayOf(like), null)
        var id = -1
        if (album != null) {
            while (album.moveToNext()) {
                id ++
                val name = album.getString(album.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
                val albumArt = album.getString(album.getColumnIndex(MediaStore.Audio.Albums._ID))
                val listAlbums = Album(id, name, albumArt)
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context))
                adapter.add(AlbumList(listAlbums))
            }
            val albumMax = album.count
            val adapterMax = adapter.itemCount
            if(adapterMax >= albumMax){
                adapter.notifyDataSetChanged()
                loadListAlbums(adapter)
            }
        }
    }

    private fun loadListAlbums(adapter: GroupAdapter<GroupieViewHolder>){
        album_reciclerView_artist_open.isVisible = true
        loading_albums_artist_open.isVisible = false
        album_reciclerView_artist_open.adapter = adapter
    }

    private fun getAlbumImage(albumId: Long): Any {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateSongs(nameArtist: String){
        val adapter = GroupAdapter<GroupieViewHolder>()
        val musicFiles= ArrayList<Song>()
        var adapterMax = 0
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val resolver = requireActivity().contentResolver
        val where = MediaStore.Audio.Media.ARTIST + " LIKE ?"
        val like: String? = nameArtist
        val song = resolver.query(uri, null, where, arrayOf(like), null)
        var id = -1
        if(song != null){
            while (song.moveToNext()){
                id ++
                val fullSongName = song.getString(song.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                val songName = fullSongName.replace(".mp3", "")
                val albumId = song.getString(song.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val albumName = song.getString(song.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artistId = song.getString(song.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                val artistName = song.getString(song.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val url = song.getString(song.getColumnIndex(MediaStore.Audio.Media.DATA))
                val songDuration = song.getString(song.getColumnIndex(MediaStore.Audio.Media.DURATION))

                val listSongs = Song(id, songName, albumId, albumName, artistId, artistName, url, songDuration)
                ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(context))
                adapter.add(SongListArtist(listSongs))
                musicFiles.add(listSongs)
            }
                val songMax = song.count
                adapterMax = adapter.itemCount
                if(adapterMax >= songMax){
                    adapter.notifyDataSetChanged()

                    loadListSongs(adapter)
                }
        }

        adapter.setOnItemClickListener { item, view ->
            val songInfo = item as SongListArtist
            val bundle = Bundle()
            bundle.putInt("id", songInfo.song.id)
            (activity as MainActivity).updateListSong(musicFiles , adapterMax)
            (activity as MainActivity).songPlayMain(bundle)
        }
    }

    private fun loadListSongs(adapter: GroupAdapter<GroupieViewHolder>){
        song_recyclerView_artist_open.isVisible = true
        loading_songs_artist_open.isVisible = false
        song_recyclerView_artist_open.adapter = adapter
    }
}

class AlbumList(val album: Album): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.album_list
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        ImageLoader.getInstance().displayImage(getAlbumImage(album.cover.toLong()).toString(),
                viewHolder.itemView.albumCover_artist_open_list, DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.ic_song)
                .resetViewBeforeLoading(true)
                .build())
    }

    private fun getAlbumImage(albumId: Long): Any {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }

}

class SongListArtist(val song: Song): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.song_list
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.songName.text = song.title
        viewHolder.itemView.songArtist.text = song.artistName
        viewHolder.itemView.songName.isSelected = true

        ImageLoader.getInstance().displayImage(getSongImage(song.albumId.toLong()).toString(),
                viewHolder.itemView.songImage, DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.ic_song)
                .resetViewBeforeLoading(true)
                .build())
    }

    private fun getSongImage(albumId: Long): Any {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }

}