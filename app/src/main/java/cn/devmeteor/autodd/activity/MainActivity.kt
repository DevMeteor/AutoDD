package cn.devmeteor.autodd.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.devmeteor.autodd.R
import cn.devmeteor.autodd.service.AlarmService
import com.blankj.utilcode.util.LogUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LogUtils.i("AutoDD Started")
    }

    fun record(view: View) {
        val intent = packageManager.getLaunchIntentForPackage("com.alibaba.android.rimet")
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val intent=Intent(this,AlarmService::class.java)
        startService(intent)
    }

}
