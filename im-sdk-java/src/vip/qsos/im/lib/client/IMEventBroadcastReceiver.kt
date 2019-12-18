package vip.qsos.im.lib.client

import vip.qsos.im.lib.client.constant.IMConstant
import vip.qsos.im.lib.client.model.Intent
import vip.qsos.im.lib.client.model.Message
import vip.qsos.im.lib.client.model.ReplyBody
import vip.qsos.im.lib.client.model.SendBody
import java.util.*

/**
 * @author : 华清松
 * 消息接收广播服务
 */
class IMEventBroadcastReceiver : IMEventListener {

    companion object {
        private var receiver: IMEventBroadcastReceiver? = null
        val instance: IMEventBroadcastReceiver
            get() {
                if (receiver == null) {
                    receiver = IMEventBroadcastReceiver()
                }
                return receiver!!
            }
    }

    /**事件监听器*/
    private var mEventListener: IMEventListener? = null
    /**连接计时器，重连计时*/
    private val mConnectionHandler = Timer()

    /**建立连接*/
    class ConnectionTask : TimerTask() {
        override fun run() {
            IMManagerHelper.connect()
        }
    }

    /**设置事件监听器*/
    fun setIMEventListener(listener: IMEventListener?) {
        this.mEventListener = listener
    }

    /**事件处理*/
    fun onReceive(intent: Intent) {
        when (intent.action) {
            IMConstant.IntentAction.ACTION_CONNECTION_CLOSED -> {
                this.onConnectionClosed()
            }
            IMConstant.IntentAction.ACTION_CONNECTION_FAILED -> {
                this.onInnerConnectionFailed(intent.getLongExtra("interval", IMConstant.RECONNECT_INTERVAL_TIME))
            }
            IMConstant.IntentAction.ACTION_CONNECTION_SUCCESS -> {
                this.onConnectionSuccess(true)
            }
            IMConstant.IntentAction.ACTION_SEND_SUCCESS -> {
                this.onSendSuccess(intent.getExtra(SendBody::class.java.name) as SendBody)
            }
            IMConstant.IntentAction.ACTION_UNCAUGHT_EXCEPTION -> {
                this.onUncaughtException(intent.getExtra(Exception::class.java.name) as Exception)
            }
            IMConstant.IntentAction.ACTION_MESSAGE_RECEIVED -> {
                this.onMessageReceived(intent.getExtra(Message::class.java.name) as Message)
            }
            IMConstant.IntentAction.ACTION_REPLY_RECEIVED -> {
                this.onReplyReceived(intent.getExtra(ReplyBody::class.java.name) as ReplyBody)
            }
            IMConstant.IntentAction.ACTION_CONNECTION_RECOVERY -> {
                IMManagerHelper.connect()
            }
        }
    }

    /**连接失败，自动重连
     * @param interval 倒计时
     * */
    private fun onInnerConnectionFailed(interval: Long) {
        mConnectionHandler.schedule(ConnectionTask(), interval)
        this.onConnectionFailed()
    }

    private fun onUncaughtException(error: Throwable?) {
        error?.printStackTrace()
    }

    override var eventDispatchOrder: Int = 0

    override fun onMessageReceived(message: Message) {
        /**服务器要求客户端下线*/
        if (IMConstant.MessageAction.ACTION_999 == message.action) {
            IMManagerHelper.stop()
        }
        mEventListener?.onMessageReceived(message)
    }

    override fun onReplyReceived(replyBody: ReplyBody) {
        mEventListener?.onReplyReceived(replyBody)
    }

    override fun onSendSuccess(sendBody: SendBody) {
        mEventListener?.onSendSuccess(sendBody)
    }

    override fun onConnectionSuccess(hasAutoBind: Boolean) {
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_CONNECTION_STATE, true)
        IMManagerHelper.autoBindDeviceId()
        mEventListener?.onConnectionSuccess(hasAutoBind)
    }

    override fun onConnectionClosed() {
        mConnectionHandler.cancel()
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_CONNECTION_STATE, false)
        mEventListener?.onConnectionClosed()
    }

    override fun onConnectionFailed() {
        mEventListener?.onConnectionFailed()
    }
}