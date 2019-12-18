import vip.qsos.im.lib.client.IMEventBroadcastReceiver
import vip.qsos.im.lib.client.IMEventListener
import vip.qsos.im.lib.client.IMManagerHelper
import vip.qsos.im.lib.client.model.SendBody

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
