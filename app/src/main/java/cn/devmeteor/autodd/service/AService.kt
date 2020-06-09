package cn.devmeteor.autodd.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import cn.devmeteor.autodd.Util
import cn.devmeteor.autodd.constant.AppConstant
import cn.devmeteor.autodd.constant.IgnoreConstant

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
                    Thread.sleep(6000)
                    val infos = rootInActiveWindow.findAccessibilityNodeInfosByText("工作台")
                    for (info in infos) {
                        info?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        info.recycle()
                    }
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
                performGlobalAction(GLOBAL_ACTION_HOME)
                Thread.sleep(500)
                Util.execShell("am force-stop com.alibaba.android.rimet")
                //小概率能打开QQ但不能进入聊天界面，原因应该出在QQ
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    //打开好友聊天界面的协议，IgnoreConstant.qqNum是好友QQ号字符串
//                    Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=${IgnoreConstant.qqNum}")
                    //打开群聊天界面的协议，IgnoreConstant.groupNum是QQ群号
                    Uri.parse("mqqwpa://im/chat?chat_type=group&uin=${IgnoreConstant.groupNum}")
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
                if (input.size != 0) {
                    input[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    input[0].performAction(AccessibilityNodeInfo.ACTION_PASTE)
                }
                val send =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/fun_btn")
                if (send.size != 0)
                    send[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
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
                tabWork = false
                smartTable = false
                fill = false
                goFill = false
                today = false
                submit = false
                qqStart = false
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
        var target = rootInActiveWindow.findAccessibilityNodeInfosByViewId(webId)[0].getChild(0)
        try {
            if (target.childCount != 0) {
                findTargetNode(target, content)
                target = targetNode
                target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                target.recycle()
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
                targetNode = child
            } else if (child.childCount > 0) {
                findTargetNode(child, content)
            }
        }
    }

}