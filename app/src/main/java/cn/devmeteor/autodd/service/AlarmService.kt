package cn.devmeteor.autodd.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import cn.devmeteor.autodd.Util
import cn.devmeteor.autodd.constant.AppConstant
import cn.devmeteor.autodd.constant.IgnoreConstant
import java.text.SimpleDateFormat
import java.util.*


class AlarmService : Service() {

    private var time:Long?=null
    private var alarmManager:AlarmManager?=null
    private var pendingIntent:PendingIntent?=null

    private val broadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("InvalidWakeLockTag")
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(AppConstant.APP_TAG, "Alarm")
            val interval:Long=3*60*1000
            time=time!!.plus(interval)
            alarmManager!!.setExact(AlarmManager.RTC_WAKEUP,time!!,pendingIntent)
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val unlocked= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                powerManager.isInteractive
            } else {
                powerManager.isScreenOn
            }
            if (unlocked)
                return
            val powerLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK,
                AppConstant.APP_TAG
            )
            powerLock.acquire(100)
            powerLock.release()
            Thread.sleep(1500)
            Util.execShell("input swipe 615 1938 615 500")  //使用shell命令执行锁屏上滑，显示输入密码页面
            Thread.sleep(1500)
            //IgnoreConstant.cmds包含点击解锁屏幕的shell命令序列和启用无障碍服务的命令"settings put secure enabled_accessibility_services cn.devmeteor.autodd/.service.AService"
            for(cmd in IgnoreConstant.cmds){
                Thread.sleep(300)
                Util.execShell(cmd)
            }
            Thread.sleep(2000)
            val ddIntent = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
            ddIntent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(ddIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter(AppConstant.ALARM_ACTION)
        registerReceiver(broadcastReceiver, intentFilter)
        time = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).parse("2020-05-03 10:36:00")!!.time
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent()
        intent.action = AppConstant.ALARM_ACTION
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val str=SimpleDateFormat("yyyy-MM-dd 00:01:00", Locale.getDefault()).format(Date())
//        time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str)!!.time
        val str=SimpleDateFormat("yyyy-MM-dd HH:mm:00", Locale.getDefault()).format(Date())
        time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str)!!.time.plus(60*1000)
        alarmManager!!.setExact(AlarmManager.RTC_WAKEUP, time!!, pendingIntent)
        println(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time!!)))
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

}