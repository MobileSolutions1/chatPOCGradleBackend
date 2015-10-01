package xmpp

/**
 * Created by marcelo on 22/09/15.
 */
public object ProcessorFactory {

    public val ACTION_REGISTER = "REGISTER"
    public val ACTION_ECHO = "ECHO"
    public val ACTION_MESSAGE = "MESSAGE"
    public val ACTION_USER_LIST = "USERLIST"
    public val ACTION_SIGNUP = "SIGNUP"
    public val ACTION_CHAT = "CHAT"
    public val ACTION_KEY_PRESS = "KEYPRESS"

    fun getProcessor(action: String?): PayloadProcessor {

        if (action == null) {
            throw IllegalStateException("action must not be null")
        }
        else if (action.equals(ACTION_REGISTER)) {
            return RegisterProcessor()
        }
        else if (action.equals(ACTION_ECHO)) {
            return EchoProcessor()
        }
        else if (action.equals(ACTION_MESSAGE)) {
            return MessageProcessor()
        }
        else if (action.equals(ACTION_SIGNUP)) {
            return UserSignup()
        }
        else if (action.equals(ACTION_USER_LIST)) {
            return UserListProcessor()
        }
        else if (action.equals(ACTION_CHAT)) {
            return ChatProcessor()
        }
        else if (action.equals(ACTION_KEY_PRESS)) {
            return UserKeyPressProcessor()
        }
        throw IllegalStateException("Action " + action + " is unknown")
    }
}
