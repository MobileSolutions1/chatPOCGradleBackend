package xmpp

import entities.CcsMessage

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class UserListProcessor: PayloadProcessor {

    private val MESSAGE_KEY = "SM"

    override fun handleMessage(msg: CcsMessage) {

        val dao = PseudoDao
        val client = CcsClient.Companion.create()
        val msgId = dao.getUniqueMessageId()
        val payload = msg.mPayload

        payload.put(msgId, msgId)
        payload.put(MESSAGE_KEY, "USERLIST")

        val userlist = dao.getAccounts()
        var users = ""
        for(user in userlist) {
            users = users + user.account + "/" + user.status + ":"
        }

        payload.put("USERLIST", users)

        val jsonRequest = client.createJsonMessage(
                msg.mFrom,
                msgId,
                payload,
                null,
                null, // TTL (null -> default-TTL)
                false)
        client.send(jsonRequest)
    }
}