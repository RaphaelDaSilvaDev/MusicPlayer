package com.bits.musicplayer.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bits.musicplayer.MainActivity
import com.bits.musicplayer.R
import com.bits.musicplayer.models.Folder
import com.bits.musicplayer.models.Song
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_folder.*
import kotlinx.android.synthetic.main.song_list.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class FolderFragment : Fragment() {
    var totalMusic = -1
    var adapter = GroupAdapter<GroupieViewHolder>()
    var musicFiles = ArrayList<Song>()
    var folders = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_folder, container, false)

        lifecycleScope.launch {
            delay(1L)
            adapter.clear()
            start()
        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun start() {
        val bundle = this.arguments
        var media: File
        if (arguments == null) {
                val appsDir = ContextCompat.getExternalFilesDirs(requireActivity(), null)
                val extRootPaths: ArrayList<File>? = ArrayList()
                for (file in appsDir) {
                    file?.parentFile?.parentFile?.parentFile?.parentFile?.let { extRootPaths?.add(it) }
                }
                if (extRootPaths != null) {
                    for (folder in extRootPaths) {
                        folders = folder.absolutePath
                        folders += "/Music"
                        media = File(folders)
                        listDir(media)
                    }
                }
            }else {
            val path = bundle?.getString("path")
            media = File(path)
            listDir(media)
        }
        AdapterClick()
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun listDir(file: File) {
        val files: Array<File>? = file.listFiles()
        var id = -1
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    id++
                    val name = file.name
                    if(name != ".thumbnails") {
                        val path = file.path
                        val dir = File(path)
                        val musicInFolder: Array<File>? = dir.listFiles()
                        var totalMusicInFolder = 0
                        if (musicInFolder != null) {
                            for (n in musicInFolder) {
                                totalMusicInFolder++
                            }
                        }
                        val folder = Folder(id, name, path, "folder", totalMusicInFolder)
                        adapter.add(FolderAdapter(folder))
                    }
                }
                if (file.isFile) {
                    if (file.extension == "mp3") {
                        id++
                        val path = file.path
                        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        val c: Cursor? = requireContext().contentResolver.query(uri, null, MediaStore.Audio.Media.DATA + " like ? ", arrayOf("%$path%"), null)
                        if (c != null) {
                            while (c.moveToNext()) {
                                totalMusic++
                                val fullName = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                                val title = fullName.replace(".mp3", "")
                                val albumId = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                                val albumName = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                                val artistId = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID))
                                val artistName = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                                val url = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA))

                                val listSongs = Song(totalMusic, title, albumId, albumName, artistId, artistName, url)
                                adapter.add(SongList(listSongs))
                                musicFiles.add(listSongs)
                            }
                        }
                    }
                }
            }
        }
        ListAdapter()
    }

    private fun ListAdapter(){
        listFolder.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun AdapterClick(){
        adapter.setOnItemClickListener { item, view ->
            val isFolder = item.toString()
            val f = isFolder.replaceAfter("@", "")
            if (f == "com.bits.musicplayer.fragments.FolderAdapter@") {
                val folderInfo = item as FolderAdapter
                val fragment: Fragment = FolderFragment()
                val bundle = Bundle()
                bundle.putInt("id", folderInfo.media.id)
                bundle.putString("name", folderInfo.media.folder)
                bundle.putString("path", folderInfo.media.path)

                fragment.arguments = bundle

                val transition = activity?.supportFragmentManager?.beginTransaction()
                transition?.replace(R.id.fragment, fragment)
                transition?.addToBackStack(null)
                transition?.commit()
            } else {
                val songInfo = item as SongList
                val bundle = Bundle()
                bundle.putInt("id", songInfo.songList.id)
                (activity as MainActivity).updateListSong(musicFiles, totalMusic + 1)
                (activity as MainActivity).songPlayMain(bundle)
            }
        }
    }
}


class FolderAdapter(val media: Folder): com.xwray.groupie.Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.song_list
    }

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.songName.text = media.folder

        if(media.extension == "folder"){
            viewHolder.itemView.songImage.setImageResource(R.drawable.ic_artist)
            viewHolder.itemView.songArtist.text = media.totalMusic.toString() + " Songs"
        }else{
            viewHolder.itemView.songImage.setImageResource(R.drawable.ic_song)
        }
    }
}