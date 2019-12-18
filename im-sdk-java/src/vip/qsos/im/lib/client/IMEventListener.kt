package vip.qsos.im.lib.client

import vip.qsos.im.lib.client.model.Message
import vip.qsos.im.lib.client.model.ReplyBody
import vip.qsos.im.lib.client.model.SendBody

/**
 * @author : 华清松
 * 消息广播监听
 */
interface IMEventListener {

    /**监听器在容器里面的排序。值越大则越先接收*/
    var eventDispatchOrder: Int

    /**
     * 监听服务端推送过来的消息时调用
     *
     * @param message
     */
    fun onMessageReceived(message: Message)

    /**
     * 监听客户端向服务端发送请求，获得服务器回执时调用
     *
     * @param replyBody 服务器回执消息体
     */
    fun onReplyReceived(replyBody: ReplyBody)

    /**
     * 监听客户端向服务端发送请求成功时
     *
     * @param sendBody 客户端消息发送实体
     */
    fun onSendSuccess(sendBody: SendBody)

    /**
     * 监听服务器连接成功时回调
     *
     * @param hasAutoBind
     * - true 表示已经自动绑定账号到服务器了，不需要再手动调用bindAccount
     */
    fun onConnectionSuccess(hasAutoBind: Boolean)

    /**监听服务器断开连接的时候回调*/
    fun onConnectionClosed()

    /**监听服务器连接失败时回调*/
    fun onConnectionFailed()
}