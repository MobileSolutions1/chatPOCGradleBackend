package xmpp

import entities.CcsMessage
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class UserSignup: PayloadProcessor {

    private val MESSAGE_KEY = "SM"
    private val logger = Logger.getLogger(this.javaClass.name)

    override fun handleMessage(msg: CcsMessage) {
        val dao = PseudoDao
        val payload = msg.mPayload
        val userName = payload.get("USER_NAME")
        logger.log(Level.INFO, "===UserSignup handleMessage() TRACE1 = " + userName)
        dao.addRegistration(msg.mFrom, userName)
   }
}