package vip.qsos.im.lib.client

import vip.qsos.im.lib.client.model.Intent
import vip.qsos.im.lib.client.model.SendBody

/**
 * @author : 华清松
 * 服务端连接服务
 */
class IMPushService {

    companion object {
        private var service: IMPushService? = null
        val instance: IMPushService
            get() {
                if (service == null) {
                    service = IMPushService()
                }
                return service!!
            }
    }

    /**处理消息事件*/
    fun onStartCommand(intent: Intent?) {
        val mIntent = intent ?: Intent(IMManagerHelper.ACTION_ACTIVATE_PUSH_SERVICE)
        when (mIntent.action) {
            IMManagerHelper.ACTION_CREATE_CONNECTION -> {
                val host: String? = IMCacheManager.instance.getString(IMCacheManager.KEY_IM_SERVER_HOST)
                val port: Int = IMCacheManager.instance.getInt(IMCacheManager.KEY_IM_SERVER_PORT)
                host?.let {
                    IMConnectorManager.instance!!.connect(host, port)
                }
            }
            IMManagerHelper.ACTION_SEND_REQUEST_BODY -> {
                IMConnectorManager.instance!!.send(mIntent.getExtra(SendBody::class.java.name) as SendBody)
            }
            IMManagerHelper.ACTION_CLOSE_CONNECTION -> {
                IMConnectorManager.instance!!.closeConnect()
            }
            IMManagerHelper.ACTION_DESTROY_CONNECTION -> {
                IMConnectorManager.instance!!.destroy()
            }
            IMManagerHelper.ACTION_ACTIVATE_PUSH_SERVICE -> {
                if (!IMConnectorManager.instance!!.isConnected) {
                    /**未连接，重连*/
                    this.onStartCommand(Intent(IMManagerHelper.ACTION_CREATE_CONNECTION))
                }
            }
        }
    }
}