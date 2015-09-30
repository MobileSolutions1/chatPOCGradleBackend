package xmpp

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class UserListProcessor: PayloadProcessor {

    private val MESSAGE_KEY = "SM"
    private val logger = Logger.getLogger(this.javaClass.name)

    override fun handleMessage(msg: CcsMessage) {

        val dao = PseudoDao
        val client = CcsClient.Companion.create()
        val msgId = dao.getUniqueMessageId()
        val payload = msg.mPayload

        payload.put(msgId, msgId)
        payload.put(MESSAGE_KEY, "USERLIST")

        val accounts = dao.getAccounts()
        var users = ""
        for(account in accounts) {
            users = users + account + ":"
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