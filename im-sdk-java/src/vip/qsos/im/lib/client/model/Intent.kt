package vip.qsos.im.lib.client.model

import java.io.Serializable
import java.util.*

/**
 * @author : 华清松
 * 事件数据实体
 */
class Intent : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    var action: String? = null
    private val data = HashMap<String, Any?>()

    constructor()
    constructor(action: String?) {
        this.action = action
    }

    fun putExtra(key: String, value: Any?) {
        data[key] = value
    }

    fun getExtra(key: String?): Any? {
        return data[key]
    }

    fun getLongExtra(key: String?, defValue: Long): Long {
        val v = getExtra(key)
        return try {
            v.toString().toLong()
        } catch (e: Exception) {
            defValue
        }
    }

}