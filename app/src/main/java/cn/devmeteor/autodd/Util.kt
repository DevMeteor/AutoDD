package cn.devmeteor.autodd

import java.io.DataOutputStream
import java.io.OutputStream

object Util {
    fun execShell(cmd: String) {
        try {
            val p = Runtime.getRuntime().exec("su")
            val outputStream: OutputStream = p.outputStream
            val dataOutputStream = DataOutputStream(outputStream)
            dataOutputStream.writeBytes(cmd)
            dataOutputStream.flush()
            dataOutputStream.close()
            outputStream.close()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}