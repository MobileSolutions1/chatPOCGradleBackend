package xmpp

import java.util.*

/**
 * Created by marcelo on 22/09/15.
 */
public data class CcsMessage(val mFrom: String, val mCategory: String, val mMessageId: String, val mPayload: HashMap<String, String>)
