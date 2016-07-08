package me.yamlee.apkrelease

import org.gradle.api.Project;

/**
 * Created by yamlee on 7/7/16.
 */
class Constants {
    /**
     * Change log file,auto generate from git log history
     */
    public static final String CHANGE_LOG_FILE_NAME = "changelog.md"
    /**
     * Channel file ,record channel list
     */
    public static final String CHANNEL_FILE_NAME = "channels.properties";
    /**
     * Release info,such as version code,last vcs commit id and so on
     */
    public static final String RELEASE_PROPERTY_FILE_NAME = "release.properties"

    public static final String FILE_CONNECTOR = "_"

    public static final String releaseFilePath(Project project) {
        return project.getRootDir().absolutePath + File.separator+ RELEASE_PROPERTY_FILE_NAME
    }

    public static final String changeLogFilePath(Project project) {
        return project.getRootDir().absolutePath + File.separator + CHANGE_LOG_FILE_NAME
    }

    public static final String channelFilePath(Project project) {
        return project.getRootDir().absolutePath + File.separator + CHANNEL_FILE_NAME
    }
}
