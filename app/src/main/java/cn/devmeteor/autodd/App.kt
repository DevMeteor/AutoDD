package cn.devmeteor.autodd

import android.app.Application
import android.os.Environment
import com.blankj.utilcode.util.LogUtils

/**
 * @author Meteor
 */
class App:Application() {
    override fun onCreate() {
        super.onCreate()
        val config=LogUtils.getConfig()
        config.isLogSwitch = true
        config.setConsoleSwitch(true)
        config.isLog2FileSwitch = true
        config.setDir(Environment.getExternalStorageDirectory())
        config.filePrefix = "AutoDD"
    }
}