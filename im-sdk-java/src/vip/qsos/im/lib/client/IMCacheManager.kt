package vip.qsos.im.lib.client

import java.util.*

/**
 * @author : 华清松
 * 消息缓存
 */
class IMCacheManager {

    fun remove(key: String?) {
        IM_CONFIG_INFO.remove(key)
    }

    fun putString(key: String, value: String?) {
        IM_CONFIG_INFO[key] = value
    }

    fun getString(key: String?): String? {
        return IM_CONFIG_INFO[key]
    }

    fun putBoolean(key: String, value: Boolean) {
        putString(key, java.lang.Boolean.toString(value))
    }

    fun getBoolean(key: String?): Boolean {
        val value = getString(key)
        return if (value == null) false else java.lang.Boolean.parseBoolean(value)
    }

    fun putInt(key: String, value: Int) {
        putString(key, value.toString())
    }

    fun getInt(key: String?): Int {
        val value = getString(key)
        return value?.toInt() ?: 0
    }

    companion object {
        /**暂存配置信息*/
        private val IM_CONFIG_INFO = HashMap<String, String?>()

        const val KEY_MANUAL_STOP = "KEY_MANUAL_STOP"
        const val KEY_IM_DESTROY = "KEY_IM_DESTROY"
        const val KEY_IM_SERVER_HOST = "KEY_IM_SERVER_HOST"
        const val KEY_IM_SERVER_PORT = "KEY_IM_SERVER_PORT"
        const val KEY_IM_CONNECTION_STATE = "KEY_IM_CONNECTION_STATE"

        var manager: IMCacheManager? = null
        val instance: IMCacheManager
            get() {
                if (manager == null) {
                    manager = IMCacheManager()
                }
                return manager!!
            }
    }
}