package com.bits.musicplayer.fragments


import android.annotation.SuppressLint
import android.content.ContentUris
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bits.musicplayer.R
import com.bits.musicplayer.models.Artist
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.artist_list.*
import kotlinx.android.synthetic.main.artist_list.view.*
import kotlinx.android.synthetic.main.fragment_artist.*
import kotlinx.android.synthetic.main.fragment_open_artist.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArtistFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_artist, container, false)

        lifecycleScope.launch {
            delay(1L)
            loadArtist()
            closeAllOpenFragments()
        }

        return view
    }

    @SuppressLint("Recycle")
    private fun loadArtist() {
        val adapter = GroupAdapter<GroupieViewHolder>()
        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        var id = -1
        val resolver = requireActivity().contentResolver
        val rs = resolver.query(uri, null, null, null, null)
        var albumArt = ""
        if (rs != null) {
            while (rs.moveToNext()) {
               id++
                val name = rs.getString(rs.getColumnIndex(MediaStore.Audio.Artists.ARTIST))
                val albumCount = rs.getString(rs.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS))
                val songCount = rs.getString(rs.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS))
                val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
                val where = MediaStore.Audio.Albums.ARTIST + " LIKE ?"
                val like: String? = name
                val cover = resolver.query(uri, null, where, arrayOf(like), null)
                if (cover != null) {
                    while (cover.moveToNext()) {
                        albumArt = cover.getString(cover.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
                    }
                }
                val listArtist = Artist(id, name, albumCount, songCount, albumArt)
                adapter.add(ArtistList(listArtist))
            }
            val rsMax = rs.count
            val adapterMax = adapter.itemCount
            if (adapterMax >= rsMax) {
                adapter.notifyDataSetChanged()
                loadListArtists(adapter)
            }
        }

        adapter.setOnItemClickListener { item, view ->
            val artistInfo = item as ArtistList
            val fragment: Fragment = OpenArtistFragment()

            val bundle = Bundle()
            bundle.putInt("id", artistInfo.artistList.id)
            bundle.putString("name", artistInfo.artistList.artistName)
            bundle.putString("albumCount", artistInfo.artistList.albumCount)
            bundle.putString("songCount", artistInfo.artistList.songCount)
            bundle.putString("artistCover", artistInfo.artistList.cover)

            fragment.arguments = bundle

            val transition = activity?.supportFragmentManager?.beginTransaction()
            transition?.replace(R.id.fragment, fragment)
            transition?.addToBackStack(null)
            transition?.commit()
        }
    }

    private fun loadListArtists(adapter: GroupAdapter<GroupieViewHolder>){
        listArtistsView.layoutManager = GridLayoutManager(context, 2)
        listArtistsView.isVisible = true
        loading_artists.isVisible = false
        listArtistsView.adapter = adapter
    }

    class ArtistList(val artistList: Artist): com.xwray.groupie.Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.artist_list
        }

        @SuppressLint("SetTextI18n")
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.artistName.text = artistList.artistName
            viewHolder.itemView.infoCountArtist.text =
                "Albums: " + artistList.albumCount + " | " + "Songs: " + artistList.songCount

            if(artistList.cover.isNotEmpty()){
                ImageLoader.getInstance().displayImage(getImage(artistList.cover.toLong()).toString(),
                        viewHolder.itemView.artistImage, DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnLoading(R.drawable.ic_artist)
                        .resetViewBeforeLoading(true)
                        .build())
            }
        }

        private fun getImage(albumId: Long): Any {
            return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
        }
    }

    private fun closeAllOpenFragments(){
        val openArtistFragment = activity?.supportFragmentManager?.findFragmentByTag("openArtistFragment")
        if (openArtistFragment != null) {
            activity?.supportFragmentManager?.beginTransaction()?.remove(openArtistFragment)?.commit()
        }
    }
}