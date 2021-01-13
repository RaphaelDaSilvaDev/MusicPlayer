package com.bits.musicplayer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Artist(var id: Int,  var artistName: String, var albumCount: String, var songCount: String, var cover: String): Parcelable {
    constructor(): this(-1, "", "", "", "")
}