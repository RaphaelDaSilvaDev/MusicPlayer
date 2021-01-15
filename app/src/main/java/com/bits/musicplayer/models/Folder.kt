package com.bits.musicplayer.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Folder(var id: Int, var folder: String, var path: String, var extension: String, var totalMusic: Int): Parcelable {
    constructor(): this(-1,"", "", "", -1)
}