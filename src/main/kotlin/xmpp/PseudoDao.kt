package xmpp

import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by marcelo on 22/09/15.
 */
public object PseudoDao {

    public val sRandom = Random()
    public val mMessageIds = HashSet<Int>()
    public val mUserMap = HashMap<String, List<String>>()
    public val mRegisteredUsers = ArrayList<String>()
    public val mNotificationKeyMap = HashMap<String, String>()

    private val logger = Logger.getLogger(this.javaClass.name)

    fun addRegistration(regId: String, accountName: String?) {
        synchronized(mRegisteredUsers) {

            logger.log(Level.INFO, "addRegistration() TRACE1 = $regId | $accountName")

            if (!mRegisteredUsers.contains(regId)) {
                logger.log(Level.INFO, "addRegistration() TRACE2")
                mRegisteredUsers.add(regId)
            }
            if (accountName != null) {
                logger.log(Level.INFO, "addRegistration() TRACE3")
                val regIdList = mUserMap.get(accountName)
                if (regIdList == null) {
                    logger.log(Level.INFO, "addRegistration() TRACE4")
                    val regIdList = ArrayList<String>()
                    mUserMap.put(accountName, regIdList.plus(regId))
                }
                logger.log(Level.INFO, "addRegistration() TRACE5")
            }
        }

        for(m in mUserMap) {
            logger.log(Level.INFO, "+++ $m")
        }
    }

    fun getAllRegistrationIds(): List<String> {
        return Collections.unmodifiableList(mRegisteredUsers)
    }

    fun getAllRegistrationIdsForAccount(account: String): List<String>? {
        val regIds = mUserMap.get(account)
        if (regIds != null) {
            return Collections.unmodifiableList(regIds)
        }
        return null
    }

    fun getNotificationKeyName(accountName: String): String? {
        return mNotificationKeyMap.get(accountName)
    }

    fun storeNotificationKeyName(accountName: String, notificationKeyName: String) {
        mNotificationKeyMap.put(accountName, notificationKeyName)
    }

    fun getAccounts(): Set<String> {
        return Collections.unmodifiableSet(mUserMap.keySet())
    }

    tailrec fun getUniqueMessageId(): String {
        val nextRandom = sRandom.nextInt()
        when (mMessageIds.contains(nextRandom)) {
            false -> return nextRandom.toString()
            true -> return getUniqueMessageId()
        }
    }
}
