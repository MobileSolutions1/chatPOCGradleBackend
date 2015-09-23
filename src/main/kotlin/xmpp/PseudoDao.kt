package xmpp

import java.util.*

/**
 * Created by marcelo on 22/09/15.
 */
public object PseudoDao {

    public val sRandom = Random()
    public val mMessageIds = HashSet<Int>()
    public val mUserMap = HashMap<String, List<String>>()
    public val mRegisteredUsers = ArrayList<String>()
    public val mNotificationKeyMap = HashMap<String, String>()

    fun addRegistration(regId: String, accountName: String?) {
        synchronized(mRegisteredUsers) {
            if (!mRegisteredUsers.contains(regId)) {
                mRegisteredUsers.add(regId)
            }
            if (accountName != null) {
                var regIdList = mUserMap.get(accountName)
                if (regIdList == null) {
                    regIdList = ArrayList<String>()
                    mUserMap.put(accountName, regIdList)
                }
                if (!regIdList.contains(regId)) {
                    regIdList += regId
                }
            }
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

    fun getUniqueMessageId(): String {
        var nextRandom = sRandom.nextInt()
        while (mMessageIds.contains(nextRandom)) {
            nextRandom = sRandom.nextInt()
        }
        return Integer.toString(nextRandom)
    }
}
