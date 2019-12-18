package vip.qsos.im.lib.client

import vip.qsos.im.lib.client.coder.IMMessageDecoder
import vip.qsos.im.lib.client.coder.IMMessageEncoder
import vip.qsos.im.lib.client.model.IProtobufAble
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Semaphore

/**
 * @author : 华清松
 * 服务端连接管理与消息处理接口
 */
interface IConnectManager {
    /**消息服务连接并发控制*/
    val mSemaphore: Semaphore
    /**消息通道*/
    var mSocketChannel: SocketChannel?
    /**消息发送编码*/
    val mMessageEncoder: IMMessageEncoder
    /**消息接收编码*/
    val mMessageDecoder: IMMessageDecoder
    /**消息读取暂存*/
    var mReadBuffer: ByteBuffer
    /**消息服务发送线程*/
    val mSocketSendExecutor: ExecutorService
    /**消息服务连接线程*/
    val mSocketConnectExecutor: ExecutorService
    /**消息服务事件广播线程*/
    val mBroadcastExecutor: ExecutorService
    /**消息服务是否已连接*/
    val isConnected: Boolean

    /**连接消息服务器*/
    fun connect(host: String, port: Int)

    /**消息通道已建立*/
    fun channelCreated()

    /**客户端发送消息*/
    fun send(body: IProtobufAble)

    /**销毁消息连接*/
    fun destroy()

    /**关闭消息会话*/
    fun closeConnect()

    /**清除资源占用*/
    fun clearAll()

    /**收到服务器消息*/
    fun messageReceived(message: Any)

    /**消息发送成功*/
    fun messageSendSuccess(message: Any)

    /**消息发送失败*/
    fun messageSendFailed(message: Any)

    /**捕获到消息服务器断开*/
    fun handelDisconnectedEvent()

    /**捕获到服务器消息数据*/
    @Throws(IOException::class)
    fun handelSocketReadEvent(result: Int)

    /**消息数据读取*/
    @Throws(IOException::class)
    fun extendByteBuffer(mReadBuffer: ByteBuffer): ByteBuffer

}
