package vip.qsos.im.lib.client.model

import vip.qsos.im.lib.client.constant.IMConstant

/**
 * @author : 华清松
 * 服务端心跳请求实体,客户端应发送一条心跳消息给服务器
 */
class HeartbeatRequest private constructor() : IProtobufAble {

    companion object {
        private const val serialVersionUID = 1L
        const val TAG = "SERVER_HEARTBEAT_REQUEST"
        const val SERVER_HEARTBEAT_REQUEST = "SR"
        val instance = HeartbeatRequest()
    }

    override val byteArray: ByteArray
        get() = SERVER_HEARTBEAT_REQUEST.toByteArray()

    override val type: Byte
        get() = IMConstant.ProtobufType.HEART_RQ

    override fun toString(): String {
        return TAG
    }

}