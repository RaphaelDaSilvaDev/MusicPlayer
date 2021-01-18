package com.bits.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi


class NotificationReceiver: BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        val actionName: String? = intent?.action
        val serviceIntent = Intent(context, MainActivity::class.java)

        if(actionName != null){
            if(context != null)
                if(actionName == "actionplay") {
                    //ac.playStopButton()
                    serviceIntent.putExtra("ActionName", "playPause")
                    context.sendBroadcast(serviceIntent)
                }else if(actionName == "actionnext") {
                    MainActivity::nextMusic
                    serviceIntent.putExtra("ActionName", "next")
                    context.sendBroadcast(serviceIntent)
                }else if(actionName == "actionprevious") {
                   // ac.previousMusic()
                   serviceIntent.putExtra("ActionName", "previous")
                   context.sendBroadcast(serviceIntent)
                }
        }
    }
}