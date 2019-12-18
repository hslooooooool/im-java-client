package vip.qsos.im.lib.client.coder

import org.slf4j.LoggerFactory
import java.nio.channels.SocketChannel

/**
 * @author : 华清松
 * 日志打印
 */
class IMLogger private constructor() {

    companion object {
        const val TAG = "IM服务"
        private val log = LoggerFactory.getLogger(IMLogger::class.java)
        val LOGGER: IMLogger by lazy { Holder.logger }
    }

    private var debug = true

    private object Holder {
        val logger = IMLogger()
    }

    fun open(mode: Boolean) {
        debug = mode
    }

    /**打印接收到的数据*/
    fun received(session: SocketChannel, message: Any) {
        if (debug) {
            log.info(TAG, String.format("[RECEIVED]" + getChannelInfo(session) + "\n%s", message))
        }
    }

    /**打印发送成功的消息*/
    fun sendSuccess(session: SocketChannel, message: Any) {
        if (debug) {
            log.info(
                TAG,
                String.format("[  SEND SUCCESS  ]" + getChannelInfo(session) + "\n%s", message)
            )
        }
    }

    /**打印发送失败的消息*/
    fun sendFailed(session: SocketChannel, message: Any) {
        if (debug) {
            log.error(
                TAG,
                String.format("[  SEND FAILED  ]" + getChannelInfo(session) + "\n%s", message)
            )
        }
    }

    /**打印发送失败的异常*/
    fun sendException(e: Exception) {
        if (debug) {
            log.error(TAG, "[  SEND EXCEPTION  ] ${e.message}")
        }
    }

    /**打印 POST 和 PORT 配置*/
    fun connectStart(host: String, port: Int) {
        if (debug) {
            log.info(TAG, "CONNECT REMOTE HOST:$host PORT:$port")
        }
    }

    /**打印无效的 POST 和 PORT 配置*/
    fun invalidHostPort(host: String?, port: Int) {
        if (debug) {
            log.error(TAG, "INVALID SOCKET ADDRESS -> HOST:$host PORT:$port")
        }
    }

    /**打印当前连接的通道信息*/
    fun connectCreated(channel: SocketChannel) {
        if (debug) {
            log.info(TAG, "[ OPENED ]" + getChannelInfo(channel))
        }
    }

    /**打印当前连接的通道信息与消息读取线程闲置时长*/
    fun connectReadIdle(channel: SocketChannel, idle: Long) {
        if (debug) {
            log.info(TAG, "[  READ IDLE  ]" + getChannelInfo(channel) + " HAS IDLE $idle ms")
        }
    }

    /**打印链接关闭并释放资源*/
    fun connectClosed(channel: SocketChannel) {
        if (debug) {
            log.info(TAG, "[ CLOSED ] ID = " + channel.hashCode())
        }
    }

    /**打印连接失败信息与重试计时毫秒数*/
    fun connectFailed(interval: Long) {
        if (debug) {
            log.warn(TAG, "CONNECT FAILURE, TRY RECONNECT AFTER " + interval + "ms")
        }
    }

    /**打印网络状态*/
    fun networkState(connect: Boolean) {
        if (debug) {
            log.info(TAG, "NETWORK IS OK = $connect")
        }
    }

    /**打印连接状态*/
    fun connectState(connected: Boolean) {
        if (debug) {
            log.info(TAG, "CONNECTED:$connected")
        }
    }

    /**打印连接状态*/
    fun connectState(connected: Boolean, manualStop: Boolean, destroyed: Boolean) {
        if (debug) {
            log.info(TAG, "CONNECTED:$connected MANUAL STOP:$manualStop DESTROYED:$destroyed")
        }
    }

    /**获取当前连接的通道信息*/
    private fun getChannelInfo(channel: SocketChannel?): String {
        val builder = StringBuilder()
        if (channel == null) {
            return ""
        }
        builder.append(" [")
        builder.append("id:").append(channel.hashCode())
        try {
            if (channel.socket().localAddress != null) {
                builder.append(" L:")
                    .append(channel.socket().localAddress.toString() + ":" + channel.socket().localPort)
            }
        } catch (ignore: Exception) {
        }

        try {
            if (channel.socket().remoteSocketAddress != null) {
                builder.append(" R:").append(channel.socket().remoteSocketAddress.toString())
            }
        } catch (ignore: Exception) {
        }

        builder.append("]")
        return builder.toString()
    }

}