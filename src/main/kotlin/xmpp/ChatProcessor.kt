package xmpp

import entities.CcsMessage

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class ChatProcessor: PayloadProcessor {

    private val MESSAGE_KEY = "SM"

    override fun handleMessage(msg: CcsMessage) {
        val dao = PseudoDao
        val client = CcsClient.Companion.create()
        val msgId = dao.getUniqueMessageId()
        val payload = msg.mPayload
        payload.put(msgId, msgId)
        payload.put(MESSAGE_KEY, "CHAT")
        val toUser = payload.get("TOUSER")
        val regIdList = dao.getAllRegistrationIdsForAccount(toUser!!)
        val jsonRequest = client.createJsonMessage(
                regIdList!!.first(),
                msgId,
                payload,
                null,
                null, // TTL (null -> default-TTL)
                false)
        client.send(jsonRequest)
    }
}