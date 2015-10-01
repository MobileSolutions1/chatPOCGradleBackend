package entities

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil

import java.io.File

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class DatabaseConfigUtil : OrmLiteConfigUtil() {
    companion object {
        @Throws(Exception::class)
        @JvmStatic fun main(args: Array<String>) {
            OrmLiteConfigUtil.writeConfigFile(File("/Users/marcelosenaga/ormlite_config.txt"), File("/Users/marcelosenaga/IdeaProjects/chatPOCGradleBackend/build/classes/main/entities"))
        }
    }
}