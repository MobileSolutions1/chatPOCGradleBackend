package entities;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;

/**
 * Created by marcelosenaga on 9/30/15.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    public static void main(String[] args) throws Exception {
        writeConfigFile(new File("/Users/marcelosenaga/ormlite_config.txt"), new File("/Users/marcelosenaga/IdeaProjects/chatPOCGradleBackend/build/classes/main/entities"));
    }
}