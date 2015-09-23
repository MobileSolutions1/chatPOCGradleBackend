package xmpp

/**
 * Created by marcelo on 22/09/15.
 */
class RegisterProcessor(): PayloadProcessor {

    override fun handleMessage(msg: CcsMessage) {
        val accountName = msg.mPayload.get("account")
        if(!accountName.isNullOrEmpty()) {
            PseudoDao.addRegistration(msg.mFrom, accountName!!)
        }
    }
}