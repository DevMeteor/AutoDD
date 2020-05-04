# AutoDD

**<span style="color:red">声明：本项目禁止用于非法用途或商用！！！</span>**

### 项目简介

本项目基于Android的shell命令、无障碍能力和定时能力实现定时自动唤醒屏幕、输入锁屏密码解锁屏幕、打开钉钉进入表单页面提交表单、进入QQ群或者个人聊天界面发送消息、再次锁屏的功能。**上述功能在本人设备上可完全实现，但在其他设备上可能由于屏幕尺寸、锁屏界面结构、钉钉内问卷入口不同而无法实现全部功能。**

其中shell命令部分实现模拟滑动锁屏页面和点击输入锁屏密码（需要root权限），无障碍（Accessibility）部分实现钉钉内提交表单和QQ内发送消息，定时部分（AlarmManager）实现在一定周期内定时执行任务。