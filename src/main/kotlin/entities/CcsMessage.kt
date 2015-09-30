package entities

import com.j256.ormlite.field.DatabaseField
import java.util.*
import com.j256.ormlite.table.DatabaseTable

/**
 * Created by marcelo on 22/09/15.
 */
@DatabaseTable(tableName = "cssmessage")
public class CcsMessage(from: String, category: String, messageId: String, payload: HashMap<String, String>) {

    constructor(): this("", "", "", HashMap<String, String>())

    @DatabaseField(generatedId = true)
    var id: Int = 0

    @DatabaseField(columnName = "from", canBeNull = true)
    var mFrom: String = from

    @DatabaseField(columnName = "category", canBeNull = true)
    var mCategory: String = category

    @DatabaseField(columnName = "messageId", canBeNull = true)
    var mMessageId: String = messageId

    var mPayload: HashMap<String, String> = payload

    override fun hashCode(): Int {
        return mFrom.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other.javaClass != this.javaClass) {
            return false;
        }
        return mFrom.equals((other as CcsMessage).mFrom);
    }
}
