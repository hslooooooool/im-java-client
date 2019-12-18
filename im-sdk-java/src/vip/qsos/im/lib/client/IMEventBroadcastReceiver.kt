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
class IMEventBroadcastReceiver {

    var random = Random()
    private var listener: IMEventListener? = null
    private val connectionHandler = Timer()
    fun setGlobalIMEventListener(ls: IMEventListener?) {
        listener = ls
    }

    fun onReceive(intent: Intent) {
        when (intent.action) {
            IMConstant.IntentAction.ACTION_CONNECTION_CLOSED -> {
                onInnerConnectionClosed()
            }
            IMConstant.IntentAction.ACTION_CONNECTION_FAILED -> {
                val interval = intent.getLongExtra("interval", IMConstant.RECONNECT_INTERVAL_TIME)
                onInnerConnectionFailed(interval)
            }
            IMConstant.IntentAction.ACTION_CONNECTION_SUCCESS -> {
                onInnerConnectionSuccess()
            }
            IMConstant.IntentAction.ACTION_MESSAGE_RECEIVED -> {
                onInnerMessageReceived(intent.getExtra(Message::class.java.name) as Message)
            }
            IMConstant.IntentAction.ACTION_REPLY_RECEIVED -> {
                listener!!.onReplyReceived(intent.getExtra(ReplyBody::class.java.name) as ReplyBody)
            }
            IMConstant.IntentAction.ACTION_SEND_SUCCESS -> {
                onSentSucceed(intent.getExtra(SendBody::class.java.name) as SendBody)
            }
            IMConstant.IntentAction.ACTION_UNCAUGHT_EXCEPTION -> {
                onUncaughtException(intent.getExtra(Exception::class.java.name) as Exception)
            }
            IMConstant.IntentAction.ACTION_CONNECTION_RECOVERY -> {
                IMManagerHelper.connect()
            }
        }
    }

    private fun onInnerConnectionClosed() {
        listener!!.onConnectionClosed()
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_CONNECTION_STATE, false)
        IMManagerHelper.connect()
    }

    private fun onInnerConnectionFailed(interval: Long) {
        connectionHandler.schedule(ConnectionTask(), interval)
        listener!!.onConnectionFailed()
    }

    private fun onInnerConnectionSuccess() {
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_CONNECTION_STATE, true)
        val autoBind = IMManagerHelper.autoBindDeviceId()
        listener!!.onConnectionSuccess(autoBind)
    }

    private fun onUncaughtException(error: Throwable?) {}

    private fun onInnerMessageReceived(message: Message) {
        if (isForceOfflineMessage(message.action)) {
            IMManagerHelper.stop()
        }
        listener!!.onMessageReceived(message)
    }

    private fun isForceOfflineMessage(action: String?): Boolean {
        return IMConstant.MessageAction.ACTION_999 == action
    }

    private fun onSentSucceed(sendBody: SendBody?) {}
    internal inner class ConnectionTask : TimerTask() {
        override fun run() {
            IMManagerHelper.connect()
        }
    }

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
}