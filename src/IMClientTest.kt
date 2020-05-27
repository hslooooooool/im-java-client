import sun.net.www.protocol.http.Handler
import vip.qsos.im.lib.client.IMEventBroadcastReceiver
import vip.qsos.im.lib.client.IMEventListener
import vip.qsos.im.lib.client.IMManagerHelper
import vip.qsos.im.lib.client.model.Message
import vip.qsos.im.lib.client.model.ReplyBody
import vip.qsos.im.lib.client.model.SendBody
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

fun main(a: Array<String>) {
    /**设置运行时参数*/
    IMManagerHelper.clientVersion = "1.0.0"
    IMManagerHelper.account = "000000001"
    /**设置全局的事件监听器*/
    IMEventBroadcastReceiver.instance.setIMEventListener(MyClientListener())
    /**连接到服务器*/
    IMManagerHelper.connect("192.168.3.131", 23456)

    val sc = Scanner(System.`in`)
    println("请输入: ")
    while (sc.hasNext()) {
        val message = "content=${sc.next()}&action=2&sender=JAVA客户端&receiver=9999&format=0"
        //doPost("http://localhost:8085/api/message/send", message)
        val send = SendBody()
        send.key = "custom"
        send.timestamp = System.currentTimeMillis()
        send.put("data", "测试数据")
        IMManagerHelper.sendRequest(send)
    }
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
            IMManagerHelper.bindAccount("000000001")
        }
    }

    override fun onMessageReceived(message: Message) {
        println(message.toString())
    }

    override fun onReplyReceived(replyBody: ReplyBody) {
        println(replyBody.toString())
    }

    override fun onSendSuccess(sendBody: SendBody) {
        println(sendBody.toString())
    }

}

fun doPost(httpUrl: String?, param: String) {
    val obj = URL(null, httpUrl, Handler())
    val con: HttpURLConnection = obj.openConnection() as HttpURLConnection
    con.requestMethod = "POST"
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
    con.setRequestProperty("Accept", "*/*")
    con.doOutput = true
    val wr = DataOutputStream(con.outputStream)
    wr.writeBytes(param)
    wr.flush()
    wr.close()

    val `in` = BufferedReader(InputStreamReader(con.inputStream))
    var inputLine: String?
    val response = StringBuffer()
    while (`in`.readLine().also { inputLine = it } != null) {
        response.append(inputLine)
    }
    `in`.close()

    println(response.toString())
}
