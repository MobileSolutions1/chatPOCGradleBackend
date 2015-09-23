package xmpp

/**
 * Created by marcelo on 22/09/15.
 */
public object ProcessorFactory {

    public val PACKAGE = "xmpp"
    public val ACTION_REGISTER = PACKAGE + ".REGISTER"
    public val ACTION_ECHO = PACKAGE + ".ECHO"
    public val ACTION_MESSAGE = PACKAGE + ".MESSAGE"

    fun getProcessor(action: String?): PayloadProcessor {
        if (action == null) {
            throw IllegalStateException("action must not be null")
        }
        if (action.equals(ACTION_REGISTER)) {
            return RegisterProcessor()
        }
        else if (action.equals(ACTION_ECHO)) {
            return EchoProcessor()
        }
        else if (action.equals(ACTION_MESSAGE)) {
            return MessageProcessor()
        }
        throw IllegalStateException("Action " + action + " is unknown")
    }
}