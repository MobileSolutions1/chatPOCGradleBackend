package xmpp

import entities.CcsMessage

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class StatusProcessor: PayloadProcessor {

    override fun handleMessage(msg: CcsMessage) {
        val dao = PseudoDao
        val msgId = dao.getUniqueMessageId()
        val payload = msg.mPayload
        payload.put(msgId, msgId)
        val toUser = payload.get("USER_NAME")
        val status = payload.get("VALUE")
        dao.setStatus(toUser!!, status!!)
    }
}