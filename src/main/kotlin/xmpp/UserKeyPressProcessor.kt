package xmpp

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class UserKeyPressProcessor: PayloadProcessor {

    private val MESSAGE_KEY = "SM"
    private val logger = Logger.getLogger(this.javaClass.name)

    override fun handleMessage(msg: CcsMessage) {

        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE1")

        val dao = PseudoDao
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE2")
        val client = CcsClient.Companion.create()
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE3")
        val msgId = dao.getUniqueMessageId()
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE4")
        val payload = msg.mPayload
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE5")
        payload.put(msgId, msgId)
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE6")

        payload.put(MESSAGE_KEY, "CHAT_KEYPRESS")
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE7")
        val toUser = payload.get("TOUSER")
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE8 = " + toUser)
        val regIdList = dao.getAllRegistrationIdsForAccount(toUser!!)
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE9")

        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE10 = " + client)
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE11 = " + regIdList!!.size)
        logger.log(Level.INFO, "===UserKeyPressProcessor handleMessage() TRACE12 = " + regIdList!!.first())

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
