package cn.devmeteor.autodd.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.devmeteor.autodd.Util
import cn.devmeteor.autodd.constant.AppConstant
import cn.devmeteor.autodd.constant.IgnoreConstant
import java.util.*

class AService : AccessibilityService() {

    private var pkgName = ""
    private var flagIndex = 0
    private var tabWork = false
    private var smartTable = false
    private var fill = false
    private var goFill = false
    private var today = false
    private var submit = false
    private var qqStart = false

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    private fun clearTimer() {
        if (timer != null)
            timer!!.cancel()
        timer = null
        if (timerTask != null)
            timerTask!!.cancel()
        timerTask = null
    }

    private fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    disableSelf()
                }
                clearTimer()
            }
        }
        timer!!.schedule(timerTask, 15000)
    }

    override fun onInterrupt() {
        Log.i(AppConstant.APP_TAG, "操作失败")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        pkgName = event!!.packageName.toString()
        println(pkgName)
        if (rootInActiveWindow == null)
            return
        if (pkgName == "com.alibaba.android.rimet") {
            if (!tabWork) {
                try {
                    Thread.sleep(10000)
//                    val infos = rootInActiveWindow.findAccessibilityNodeInfosByText("工作台")
                    val info =
                        rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/home_app_recycler_view")[0]
                    info.getChild(2).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    info.recycle()
                    tabWork = true
                    Thread.sleep(5000)
                } catch (e: Exception) {
                }
            }
            //网页内文字已属于text，但使用findAccessibilityNodeInfosByText找不到目标组件，原因未知
            if (tabWork && !smartTable)
                click("智能填表", "com.alibaba.android.rimet:id/h5_pc_container")
            if (smartTable && !fill)
                click("填写")
            if (fill && !goFill)
                click("立即填写")
            if (goFill && !today)
                click("今天")
            if (today && !submit)
                click("提交")
            if (submit && !qqStart) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
                clearTimer()
                val info =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/home_app_recycler_view")[0]
                info.getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                info.recycle()
                Thread.sleep(500)
                val group =
                    rootInActiveWindow.findAccessibilityNodeInfosByText(IgnoreConstant.groupName)[0]
                val rect = Rect()
                group.getBoundsInScreen(rect)
                group.recycle()
                Util.execShell("input tap ${rect.centerX()} ${rect.centerY()}")
                Thread.sleep(500)
                val more =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/add_app")[0]
                more.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                more.recycle()
                Thread.sleep(500)
                val viewPager =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.alibaba.android.rimet:id/chat_app_pager")[0]
                viewPager.getBoundsInScreen(rect)
                viewPager.recycle()
                Util.execShell("input swipe ${rect.centerX() + 500} ${rect.centerY()} ${rect.centerX() - 500} ${rect.centerY()}")
                Thread.sleep(2000)
                val item = rootInActiveWindow.findAccessibilityNodeInfosByText("签到")
                for (view in item) {
                    if (view.viewIdResourceName == "com.alibaba.android.rimet:id/chat_app_button_title") {
                        view.parent.getBoundsInScreen(rect)
                        Util.execShell("input tap ${rect.centerX()} ${rect.centerY()}")
                        Thread.sleep(500)
                    }
                    view.recycle()
                }
                val org =
                    rootInActiveWindow.findAccessibilityNodeInfosByText(IgnoreConstant.orgName)[0]
                org.getBoundsInScreen(rect)
                org.recycle()
                Util.execShell("input tap ${rect.centerX()} ${rect.centerY()}")
                Thread.sleep(2000)
                rootInActiveWindow.getBoundsInScreen(rect)
                Util.execShell("input tap ${rect.centerX()} ${rect.centerY()}")
                Thread.sleep(1000)
                rootInActiveWindow.getBoundsInScreen(rect)
                Util.execShell("input tap ${rect.centerX()} ${rect.bottom - 200}")
                Thread.sleep(500)
                performGlobalAction(GLOBAL_ACTION_HOME)
                Thread.sleep(500)
                Util.execShell("am force-stop com.alibaba.android.rimet")
                //小概率能打开QQ但不能进入聊天界面，原因应该出在QQ
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    //打开好友聊天界面的协议，IgnoreConstant.qqNum是好友QQ号字符串
                    Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${IgnoreConstant.qqNum}")
                    //打开群聊天界面的协议，IgnoreConstant.groupNum是QQ群号
//                    Uri.parse("mqqwpa://im/chat?chat_type=group&uin=${IgnoreConstant.groupNum}")
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                qqStart = true
                Thread.sleep(5000)
            }
        }
        if (pkgName == "com.tencent.mobileqq") {
            if (qqStart) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", "无异常")
                clipboardManager.setPrimaryClip(clipData)
                val input =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/input")
                for (i in input) {
                    i.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                    i.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                    i.recycle()
                }
                val send =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/fun_btn")
                if (send.size != 0)
                    send[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                send[0]?.recycle()
                Thread.sleep(1500)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(1000)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(1500)
                performGlobalAction(GLOBAL_ACTION_HOME)
                Thread.sleep(1500)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    disableSelf()
                }
            }
        }
    }

    private fun click(
        content: String,
        webId: String = "com.alibaba.android.rimet:id/webview_frame",
        sleepTime: Long = 5000
    ) {
        if (timer == null)
            startTimer()
        var target = rootInActiveWindow.findAccessibilityNodeInfosByViewId(webId)[0].getChild(0)
        try {
            if (target.childCount != 0) {
                findTargetNode(target, content)
                target = targetNode
                target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                target.recycle()
                println(timer == null)
                Thread.sleep(sleepTime)
                when (flagIndex) {
                    0 -> smartTable = true
                    1 -> fill = true
                    2 -> goFill = true
                    3 -> today = true
                    4 -> submit = true
                }
                flagIndex++
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private lateinit var targetNode: AccessibilityNodeInfo

    private fun findTargetNode(rootNode: AccessibilityNodeInfo, content: String) {
        for (i in 0 until rootNode.childCount) {
            val child = rootNode.getChild(i)
            if (content == "${child.text}") {
                clearTimer()
                targetNode = child
            } else if (child.childCount > 0) {
                findTargetNode(child, content)
            }
        }
    }

}