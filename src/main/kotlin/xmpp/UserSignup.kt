package xmpp

import entities.CcsMessage

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class UserSignup: PayloadProcessor {

    override fun handleMessage(msg: CcsMessage) {
        val dao = PseudoDao
        val payload = msg.mPayload
        val userName = payload.get("USER_NAME")
        dao.addRegistration(msg.mFrom, userName)
   }
}