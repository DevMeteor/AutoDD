package cn.devmeteor.autodd.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.devmeteor.autodd.service.AlarmService


class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals("android.intent.action.BOOT_COMPLETED")) {
            val serviceIntent=Intent(context, AlarmService::class.java)
            context!!.startService(serviceIntent)
        }
    }
}