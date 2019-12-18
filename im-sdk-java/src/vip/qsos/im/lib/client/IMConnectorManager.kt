package vip.qsos.im.lib.client

import vip.qsos.im.lib.client.coder.IMLogger
import vip.qsos.im.lib.client.coder.IMMessageDecoder
import vip.qsos.im.lib.client.coder.IMMessageEncoder
import vip.qsos.im.lib.client.constant.IMConstant
import vip.qsos.im.lib.client.model.*
import java.io.IOException
import java.lang.Runnable
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 * @author : 华清松
 * 服务端连接管理与消息处理
 */
class IMConnectorManager : IConnectManager {
    companion object {
        @get:Synchronized
        var instance: IMConnectorManager? = null
            get() {
                if (field == null) {
                    field = IMConnectorManager()
                }
                return field!!
            }
            private set

        const val READ_BUFFER_SIZE = 2048
        const val WRITE_BUFFER_SIZE = 1024
        /**连接服务器的超时时长，毫秒*/
        const val CONNECT_TIME_OUT = 10 * 1000
    }

    override val mSemaphore: Semaphore = Semaphore(1, true)
    override var mSocketChannel: SocketChannel? = null
    override val mMessageEncoder: IMMessageEncoder = IMMessageEncoder()
    override val mMessageDecoder: IMMessageDecoder = IMMessageDecoder()
    override var mReadBuffer: ByteBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE)

    override val mSocketSendExecutor: ExecutorService = Executors.newFixedThreadPool(1) { r ->
        Thread(r, "SocketSend-")
    }
    override val mSocketConnectExecutor: ExecutorService = Executors.newFixedThreadPool(1) { r ->
        Thread(r, "SocketConnect-")
    }

    override val mBroadcastExecutor: ExecutorService = Executors.newFixedThreadPool(1) { r ->
        Thread(r, "SocketBroadcast-")
    }

    override val isConnected: Boolean
        get() = this.mSocketChannel != null && this.mSocketChannel!!.isConnected

    override fun connect(host: String, port: Int) {
        if (isConnected) {
            return
        }
        this.mSocketConnectExecutor.execute(Runnable {
            if (isConnected) {
                return@Runnable
            }
            IMLogger.LOGGER.connectStart(host, port)
            IMCacheManager.instance.putBoolean(IMCacheManager.KEY_IM_CONNECTION_STATE, false)
            try {
                this.mSemaphore.acquire()
                this.mSocketChannel = SocketChannel.open()
                this.mSocketChannel!!.configureBlocking(true)
                this.mSocketChannel!!.socket().tcpNoDelay = true
                this.mSocketChannel!!.socket().keepAlive = true
                this.mSocketChannel!!.socket().receiveBufferSize = READ_BUFFER_SIZE
                this.mSocketChannel!!.socket().sendBufferSize = WRITE_BUFFER_SIZE
                this.mSocketChannel!!.socket().connect(InetSocketAddress(host, port), CONNECT_TIME_OUT)
                this.mSemaphore.release()

                this.channelCreated()

                var result = 1
                while (result > 0) {
                    result = this.mSocketChannel!!.read(this.mReadBuffer)
                    if (result > 0) {
                        if (this.mReadBuffer.position() == this.mReadBuffer.capacity()) {
                            mReadBuffer = extendByteBuffer(this.mReadBuffer)
                        }
                        this.handelSocketReadEvent(result)
                    }
                }
                this.handelSocketReadEvent(result)
            } catch (ignore: ConnectException) {
                this.mSemaphore.release()
                this.handleConnectAbortedEvent()
            } catch (ignore: SocketTimeoutException) {
                this.mSemaphore.release()
                this.handleConnectAbortedEvent()
            } catch (ignore: IOException) {
                this.mSemaphore.release()
                this.handelDisconnectedEvent()
            } catch (ignore: InterruptedException) {
                this.mSemaphore.release()
            } finally {
                this.mSemaphore.release()
            }
        })
    }

    override fun handelDisconnectedEvent() {
        this.closeConnect()
    }

    private fun handleConnectAbortedEvent() {
        val interval: Long = IMConstant.RECONNECT_INTERVAL_TIME - (5 * 1000 - Random().nextInt(15 * 1000))
        IMLogger.LOGGER.connectFailed(interval)
        val intent = Intent()
        intent.action = IMConstant.IntentAction.ACTION_CONNECTION_FAILED
        intent.putExtra("interval", interval)
        sendBroadcast(intent)
    }

    @Throws(IOException::class)
    override fun handelSocketReadEvent(result: Int) {
        if (result == -1) {
            this.closeConnect()
            return
        }
        this.mReadBuffer.position(0)
        val message = this.mMessageDecoder.decode(this.mReadBuffer) ?: return
        IMLogger.LOGGER.received(this.mSocketChannel!!, message)

        if (message is HeartbeatRequest) {
            send(HeartbeatResponse.instance)
            return
        }
        messageReceived(message)
    }

    override fun extendByteBuffer(mReadBuffer: ByteBuffer): ByteBuffer {
        val newBuffer = ByteBuffer.allocate(mReadBuffer.capacity() + READ_BUFFER_SIZE / 2)
        mReadBuffer.position(0)
        newBuffer.put(this.mReadBuffer)
        mReadBuffer.clear()
        return newBuffer
    }

    override fun send(body: IProtobufAble) {
        if (!isConnected) {
            this.messageSendFailed(body)
            return
        }
        this.mSocketSendExecutor.execute {
            var result = 0
            try {
                this.mSemaphore.acquire()
                val buffer = this.mMessageEncoder.encode(body)
                while (buffer.hasRemaining()) {
                    result += this.mSocketChannel!!.write(buffer)
                }
            } catch (e: Exception) {
                IMLogger.LOGGER.sendException(e)
                result = -1
            } finally {
                this.mSemaphore.release()
                if (result <= 0) {
                    this.closeConnect()
                } else {
                    this.messageSendSuccess(body)
                }
            }
        }
    }

    override fun channelCreated() {
        IMLogger.LOGGER.connectCreated(this.mSocketChannel!!)
        val intent = Intent()
        intent.action = IMConstant.IntentAction.ACTION_CONNECTION_SUCCESS
        this.sendBroadcast(intent)
    }

    override fun clearAll() {
        IMLogger.LOGGER.connectClosed(this.mSocketChannel!!)
        this.mReadBuffer.clear()
        this.mSemaphore.release()
        if (this.mReadBuffer.capacity() > READ_BUFFER_SIZE) {
            this.mReadBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE)
        }
        val intent = Intent()
        intent.action = IMConstant.IntentAction.ACTION_CONNECTION_CLOSED
        this.sendBroadcast(intent)
    }

    override fun messageReceived(message: Any) {
        if (message is Message) {
            val intent = Intent()
            intent.action = IMConstant.IntentAction.ACTION_MESSAGE_RECEIVED
            intent.putExtra(Message::class.java.name, message as Message?)
            this.sendBroadcast(intent)
        }
        if (message is ReplyBody) {
            val intent = Intent()
            intent.action = IMConstant.IntentAction.ACTION_REPLY_RECEIVED
            intent.putExtra(ReplyBody::class.java.name, message as ReplyBody?)
            this.sendBroadcast(intent)
        }
    }

    override fun messageSendSuccess(message: Any) {
        IMLogger.LOGGER.sendSuccess(this.mSocketChannel!!, message)
        if (message is SendBody) {
            val intent = Intent()
            intent.action = IMConstant.IntentAction.ACTION_SEND_SUCCESS
            intent.putExtra(SendBody::class.java.name, message as SendBody?)
            this.sendBroadcast(intent)
        }
    }

    override fun messageSendFailed(message: Any) {
        IMLogger.LOGGER.sendFailed(this.mSocketChannel!!, message)
        if (message is SendBody) {
            val intent = Intent()
            intent.action = IMConstant.IntentAction.ACTION_SEND_FAILED
            intent.putExtra(SendBody::class.java.name, message as SendBody?)
            this.sendBroadcast(intent)
        }
    }

    override fun destroy() {
        closeConnect()
    }

    override fun closeConnect() {
        if (!isConnected) {
            return
        }
        try {
            this.mSocketChannel!!.close()
        } catch (ignore: IOException) {
        } finally {
            this.clearAll()
        }
    }

    private fun sendBroadcast(intent: Intent) {
        mBroadcastExecutor.execute {
            IMEventBroadcastReceiver.instance.onReceive(intent)
        }
    }

}