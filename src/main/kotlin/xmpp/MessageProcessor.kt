package xmpp

import entities.CcsMessage

/**
 * Created by marcelo on 22/09/15.
 */
public class MessageProcessor(): PayloadProcessor {
    override fun handleMessage(msg: CcsMessage) {
        val dao = PseudoDao
        val client = CcsClient.Companion.create()
        val msgId = dao.getUniqueMessageId()
        val jsonRequest = client.createJsonMessage(
                msg.mFrom,
                msgId,
                msg.mPayload,
                null,
                null, // TTL (null -> default-TTL)
                false)
        client.send(jsonRequest)
    }
}