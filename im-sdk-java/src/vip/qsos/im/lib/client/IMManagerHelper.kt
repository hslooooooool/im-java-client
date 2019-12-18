package vip.qsos.im.lib.client

import vip.qsos.im.lib.client.constant.IMConstant
import vip.qsos.im.lib.client.model.Intent
import vip.qsos.im.lib.client.model.SendBody
import java.util.*

/**
 * @author : 华清松
 * 消息服务管理帮助类
 */
object IMManagerHelper {
    /**【动作】发送消息到服务器*/
    const val ACTION_SEND_REQUEST_BODY = "ACTION_SEND_REQUEST_BODY"
    /**【动作】关闭服务器连接*/
    const val ACTION_CLOSE_CONNECTION = "ACTION_CLOSE_CONNECTION"
    /**【动作】销毁服务器连接*/
    const val ACTION_DESTROY_CONNECTION = "ACTION_DESTROY_CONNECTION"
    /**【动作】连接消息服务器*/
    const val ACTION_CREATE_CONNECTION = "ACTION_CREATE_CONNECTION"
    /**【动作】消息服务器活跃检测，死掉将重连*/
    const val ACTION_ACTIVATE_PUSH_SERVICE = "ACTION_ACTIVATE_PUSH_SERVICE"
    /**发送的消息数据*/
    const val KEY_SEND_BODY = "KEY_SEND_BODY"
    /**连接状态*/
    const val KEY_CONNECTION_STATUS = "KEY_CONNECTION_STATUS"

    /**服务连接状态*/
    enum class STATE {
        NORMAL,
        STOP,
        DESTROY;
    }

    /**连接服务端，在程序启动页或者 在Application里调用*/
    fun connect(host: String?, port: Int) {
        if (host == null || host.trim().isEmpty()) {
            return
        }
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_DESTROY, false)
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_MANUAL_STOP, false)
        IMCacheManager.instance.putString(IMCacheManager.KEY_IM_SERVER_HOST, host)
        IMCacheManager.instance.putInt(IMCacheManager.KEY_IM_SERVER_PORT, port)
        val serviceIntent = Intent()
        serviceIntent.putExtra(IMCacheManager.KEY_IM_SERVER_HOST, host)
        serviceIntent.putExtra(IMCacheManager.KEY_IM_SERVER_PORT, port)
        serviceIntent.action = this.ACTION_CREATE_CONNECTION
        postEvent(serviceIntent)
    }

    /**连接服务器，从缓存中获取host和port*/
    fun connect() {
        val manualStop: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_MANUAL_STOP)
        val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
        if (manualStop || manualDestroy) {
            return
        }
        val host: String? = IMCacheManager.instance.getString(IMCacheManager.KEY_IM_SERVER_HOST)
        val port: Int = IMCacheManager.instance.getInt(IMCacheManager.KEY_IM_SERVER_PORT)
        connect(host, port)
    }

    /**账号登录*/
    fun bindAccount(account: String?) {
        val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
        if (manualDestroy || account == null || account.trim().isEmpty()) {
            return
        }
        sendBindRequest(account)
    }

    /**账号自动登录*/
    fun autoBindDeviceId(): Boolean {
        val account: String? = account
        val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
        val manualStop: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_MANUAL_STOP)
        if (manualStop || account == null || account.trim().isEmpty() || manualDestroy) {
            return false
        }
        sendBindRequest(account)
        return true
    }

    /**发送消息*/
    fun sendRequest(body: SendBody?) {
        val manualStop: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_MANUAL_STOP)
        val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
        if (manualStop || manualDestroy) {
            return
        }
        val serviceIntent = Intent()
        serviceIntent.putExtra(SendBody::class.java.name, body)
        serviceIntent.action = ACTION_SEND_REQUEST_BODY
        postEvent(serviceIntent)
    }

    /**停止服务，退出当前账号登录，断开连接，可重连*/
    fun stop() {
        val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
        if (manualDestroy) {
            return
        }
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_MANUAL_STOP, true)
        postEvent(Intent(this.ACTION_CLOSE_CONNECTION))
    }

    /**完全销毁，一般用于完全退出程序，调用resume将不能恢复*/
    fun destroy() {
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_DESTROY, true)
        val serviceIntent = Intent()
        serviceIntent.action = this.ACTION_DESTROY_CONNECTION
        postEvent(serviceIntent)
    }

    /**重新恢复接收推送，重新连接服务端并登录当前账号*/
    fun resume() {
        val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
        if (manualDestroy) {
            return
        }
        autoBindDeviceId()
    }

    /**是否已连接*/
    val isConnected: Boolean
        get() = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_CONNECTION_STATE)

    /**连接状态*/
    val state: STATE
        get() {
            val manualDestroy: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_IM_DESTROY)
            val manualStop: Boolean = IMCacheManager.instance.getBoolean(IMCacheManager.KEY_MANUAL_STOP)
            if (manualDestroy) {
                return STATE.DESTROY
            }
            return if (manualStop) STATE.STOP else STATE.NORMAL
        }

    /**连接版本*/
    var clientVersion: String
        get() = System.getProperties().getProperty(IMConstant.ConfigKey.CLIENT_VERSION)
        set(version) {
            System.getProperties()[IMConstant.ConfigKey.CLIENT_VERSION] = version
        }

    /**登录账号*/
    var account: String
        get() = System.getProperties().getProperty(IMConstant.ConfigKey.CLIENT_ACCOUNT)
        set(account) {
            System.getProperties()[IMConstant.ConfigKey.CLIENT_ACCOUNT] = account
        }

    /**发布事件*/
    private fun postEvent(intent: Intent) {
        IMPushService.instance.onStartCommand(intent)
    }

    /**账号登录*/
    private fun sendBindRequest(account: String) {
        IMCacheManager.instance.putBoolean(IMCacheManager.KEY_MANUAL_STOP, false)
        val sendBody = SendBody()
        val sysPro = System.getProperties()
        sendBody.key = IMConstant.RequestKey.CLIENT_BIND
        sendBody.put("account", account)
        sendBody.put("deviceId", deviceId)
        sendBody.put("channel", "java")
        sendBody.put("device", sysPro.getProperty("os.name"))
        sendBody.put("version", clientVersion)
        sendBody.put("osVersion", sysPro.getProperty("os.version"))
        sendRequest(sendBody)
    }

    /**生成设备唯一ID*/
    private val deviceId: String
        get() {
            var deviceId = System.getProperties().getProperty(IMConstant.ConfigKey.CLIENT_DEVICE_ID)
            if (deviceId == null) {
                deviceId = UUID.randomUUID().toString().replace("-".toRegex(), "").toUpperCase()
                System.getProperties()[IMConstant.ConfigKey.CLIENT_DEVICE_ID] = deviceId
            }
            return deviceId
        }
}