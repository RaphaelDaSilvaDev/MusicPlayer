package com.bits.musicplayer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Song(var id: Int, var title: String, var albumId: String, var albumName: String,
           var artistId: String, var artistName: String, var url: String): Parcelable{
constructor() : this(-1,"", "", "", "","", "")
}