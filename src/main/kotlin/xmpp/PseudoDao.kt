package xmpp

import entities.User
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by marcelo on 22/09/15.
 */
public object PseudoDao {

    public val sRandom = Random()
    public val mMessageIds = HashSet<Int>()
    public val mUserMap = hashMapOf<String, User>()
    public val mRegisteredUsers = ArrayList<String>()
    public val mNotificationKeyMap = hashMapOf<String, String>()

    private val logger = Logger.getLogger(this.javaClass.name)

    fun addRegistration(regId: String, accountName: String?) {
        synchronized(mRegisteredUsers) {
            if (!mRegisteredUsers.contains(regId)) {
                mRegisteredUsers.add(regId)
            }
            if (accountName != null) {
                val user = mUserMap.get(accountName)
                if (user == null) {
                    mUserMap.put(accountName, User(regId, accountName, "online"))
                }
            }
            val user = mUserMap.get(accountName)
            user?.status = "online"
        }
    }

    fun getAllRegistrationIds(): List<String> {
        return Collections.unmodifiableList(mRegisteredUsers)
    }

    fun getAllRegistrationIdsForAccount(account: String): String? {
        val user = mUserMap.get(account)
        return user?.regId
    }

    fun setStatus(account: String, status: String) {
        val user = mUserMap.get(account)
        user?.status = status
    }

    fun getNotificationKeyName(accountName: String): String? {
        return mNotificationKeyMap.get(accountName)
    }

    fun storeNotificationKeyName(accountName: String, notificationKeyName: String) {
        mNotificationKeyMap.put(accountName, notificationKeyName)
    }

    fun getAccounts(): Collection<User> {
        return mUserMap.values()
    }

    tailrec fun getUniqueMessageId(): String {
        val nextRandom = sRandom.nextInt()
        when (mMessageIds.contains(nextRandom)) {
            false -> return nextRandom.toString()
            true -> return getUniqueMessageId()
        }
    }
}
