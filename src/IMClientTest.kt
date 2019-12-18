import vip.qsos.im.lib.client.IMEventBroadcastReceiver
import vip.qsos.im.lib.client.IMEventListener
import vip.qsos.im.lib.client.IMManagerHelper
import vip.qsos.im.lib.client.model.SendBody
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

fun main(a: Array<String>) {
    /**设置运行时参数*/
    IMManagerHelper.clientVersion = "1.0.0"
    IMManagerHelper.account = "JAVA客户端"
    /**设置全局的事件监听器*/
    IMEventBroadcastReceiver.instance.setIMEventListener(MyClientListener())
    /**连接到服务器*/
    IMManagerHelper.connect("127.0.0.1", 23456)
}

/**
 * @author : 华清松
 * 消息监听器
 */
class MyClientListener : IMEventListener {

    override var eventDispatchOrder: Int = 100

    override fun onConnectionClosed() {
        println("onConnectionClosed")
    }

    override fun onConnectionFailed() {
        println("onConnectionFailed")
    }

    override fun onConnectionSuccess(hasAutoBind: Boolean) {
        println("onConnectionSuccess")
        if (!hasAutoBind) {
            IMManagerHelper.bindAccount("10000")
        }
    }

    override fun onMessageReceived(message: vip.qsos.im.lib.client.model.Message) {
        println(message.toString())
    }

    override fun onReplyReceived(replyBody: vip.qsos.im.lib.client.model.ReplyBody) {
        println(replyBody.toString())
    }

    override fun onSendSuccess(sendBody: SendBody) {
        println(sendBody.toString())
    }

}

fun doPost(httpUrl: String?, param: String): String? {
    var connection: HttpURLConnection? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    var bufferedReader: BufferedReader? = null
    var result: String? = null
    try {
        val url = URL(httpUrl)
        // 通过远程url连接对象打开连接
        connection = url.openConnection() as HttpURLConnection
        // 设置连接请求方式
        connection.requestMethod = "POST"
        // 设置连接主机服务器超时时间：1000毫秒
        connection.connectTimeout = 1000
        // 设置读取主机服务器返回数据超时时间：2000毫秒
        connection.readTimeout = 2000
        // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
        connection.doOutput = true
        // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
        connection.doInput = true
        // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        // 通过连接对象获取一个输出流
        outputStream = connection.outputStream
        // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
        outputStream.write(param.toByteArray())
        // 通过连接对象获取一个输入流，向远程读取
        if (connection.responseCode == 200) {
            inputStream = connection.inputStream
            // 对输入流对象进行包装:charset根据工作项目组的要求来设置
            bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val sbf = StringBuffer()
            var temp: String? = null
            // 循环遍历一行一行读取数据
            while (bufferedReader.readLine().also { temp = it } != null) {
                sbf.append(temp)
                sbf.append("\r\n")
            }
            result = sbf.toString()
        }
    } catch (e: MalformedURLException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        // 关闭资源
        if (null != bufferedReader) {
            try {
                bufferedReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (null != outputStream) {
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (null != inputStream) {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        // 断开与远程地址url的连接
        connection?.disconnect()
    }
    return result
}
