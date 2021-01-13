package com.bits.musicplayer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Album(var id: Int, var name: String, var cover: String):Parcelable {
    constructor(): this(-1,"","")
}