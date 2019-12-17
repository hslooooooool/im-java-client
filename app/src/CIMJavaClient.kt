import com.farsunset.cim.sdk.client.CIMEventBroadcastReceiver
import com.farsunset.cim.sdk.client.CIMEventListener
import com.farsunset.cim.sdk.client.CIMPushManager
import com.farsunset.cim.sdk.client.model.Message
import com.farsunset.cim.sdk.client.model.ReplyBody

fun main(a: Array<String>) {
    /**设置运行时参数*/
    CIMPushManager.setClientVersion("1.0.0")
    /**设置全局的事件监听器*/
    CIMEventBroadcastReceiver.getInstance().setGlobalCIMEventListener(CIMJavaClient())
    /**连接到服务器*/
    CIMPushManager.connect("127.0.0.1", 23456)
}

/**
 * @author : 华清松
 * 消息监听器
 */
class CIMJavaClient : CIMEventListener {
    override fun onConnectionClosed() {
        println("onConnectionClosed")
    }

    override fun onConnectionFailed() {
        println("onConnectionFailed")
    }

    override fun onConnectionSuccessed(hasAutoBind: Boolean) {
        println("onConnectionSuccess")
        if (!hasAutoBind) {
            CIMPushManager.bindAccount("10000")
        }
    }

    override fun onMessageReceived(message: Message) {
        println(message.toString())
    }

    override fun onReplyReceived(replybody: ReplyBody) {
        println(replybody.toString())
    }

    override fun getEventDispatchOrder(): Int { // TODO Auto-generated method stub
        return 0
    }

}
