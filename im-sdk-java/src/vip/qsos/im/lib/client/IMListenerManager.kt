package vip.qsos.im.lib.client

import org.slf4j.LoggerFactory
import vip.qsos.im.lib.client.model.Message
import vip.qsos.im.lib.client.model.ReplyBody
import java.util.*

/**
 * CIM 消息监听器管理
 */
object IMListenerManager {
    private val cimListeners = ArrayList<IMEventListener>()
    private val comparator = CIMMessageReceiveComparator()
    private val LOGGER = LoggerFactory.getLogger(IMListenerManager::class.java)

    fun registerMessageListener(listener: IMEventListener) {
        if (!cimListeners.contains(listener)) {
            cimListeners.add(listener)
            Collections.sort(cimListeners, comparator)
        }
    }

    fun removeMessageListener(listener: IMEventListener) {
        for (i in cimListeners.indices) {
            if (listener.javaClass == cimListeners[i].javaClass) {
                cimListeners.removeAt(i)
            }
        }
    }

    fun notifyOnConnectionSuccess(autoBind: Boolean) {
        for (listener in cimListeners) {
            listener.onConnectionSuccess(autoBind)
        }
    }

    fun notifyOnMessageReceived(message: Message) {
        for (listener in cimListeners) {
            listener.onMessageReceived(message)
        }
    }

    fun notifyOnConnectionClose() {
        for (listener in cimListeners) {
            listener.onConnectionClosed()
        }
    }

    fun notifyOnReplyReceived(body: ReplyBody) {
        for (listener in cimListeners) {
            listener.onReplyReceived(body)
        }
    }

    fun notifyOnConnectionFailed() {
        for (listener in cimListeners) {
            listener.onConnectionFailed()
        }
    }

    fun destroy() {
        cimListeners.clear()
    }

    fun logListenersName() {
        for (listener in cimListeners) {
            LOGGER.debug("#######" + listener.javaClass.name + "#######")
        }
    }

    /**消息接收顺序排序，eventDispatchOrder 倒序*/
    private class CIMMessageReceiveComparator :
        Comparator<IMEventListener> {
        override fun compare(
            arg1: IMEventListener,
            arg2: IMEventListener
        ): Int {
            val order1 = arg1.eventDispatchOrder
            val order2 = arg2.eventDispatchOrder
            return order2 - order1
        }
    }
}