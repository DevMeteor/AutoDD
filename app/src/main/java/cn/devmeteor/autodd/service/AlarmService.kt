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
import com.blankj.utilcode.util.LogUtils
import java.text.SimpleDateFormat
import java.util.*


class AlarmService : Service() {

    private var time:Long?=null
    private var alarmManager:AlarmManager?=null
    private var pendingIntent:PendingIntent?=null
    private var wakeLock:PowerManager.WakeLock?=null

    private val broadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("InvalidWakeLockTag")
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(AppConstant.APP_TAG, "Alarm")
            LogUtils.i("Alarm Triggered")
//            val interval:Long=10*60*1000
            val interval:Long=AlarmManager.INTERVAL_DAY
            time=time!!.plus(interval)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager!!.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time!!,pendingIntent)
            }else
                alarmManager!!.setExact(AlarmManager.RTC_WAKEUP,time!!,pendingIntent)
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val unlocked= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                powerManager.isInteractive
            } else {
                powerManager.isScreenOn
            }
            if (unlocked)
                return
            LogUtils.i("command executing...")
            Util.execShell("input keyevent 26")
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

    @SuppressLint("InvalidWakeLockTag", "WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        val powerManager=getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"AutoDD.AlarmService")
        wakeLock!!.acquire()
        LogUtils.i("AlarmServiceStarted")
        val intentFilter = IntentFilter(AppConstant.ALARM_ACTION)
        registerReceiver(broadcastReceiver, intentFilter)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent()
        intent.action = AppConstant.ALARM_ACTION
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
//        val str=SimpleDateFormat("yyyy-MM-dd 06:30:00", Locale.getDefault()).format(Date())
        val str=SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.getDefault()).format(Date())
        time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str)!!.time.plus(AlarmManager.INTERVAL_DAY)
//        val str=SimpleDateFormat("yyyy-MM-dd HH:mm:00", Locale.getDefault()).format(Date())
//        time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str)!!.time.plus(60*1000)
//        time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str)!!.time.plus(4*60*60*1000)
//        time=SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str)!!.time.plus(AlarmManager.INTERVAL_HALF_DAY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager!!.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time!!, pendingIntent)
        }else
            alarmManager!!.setExact(AlarmManager.RTC_WAKEUP, time!!, pendingIntent)
        println(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time!!)))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        if (wakeLock!=null)
            wakeLock!!.release()
    }

}