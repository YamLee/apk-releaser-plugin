package me.yamlee.apkrelease.internel

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.internel.vcs.LogMessage
import me.yamlee.apkrelease.internel.vcs.VcsOperator
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

/**
 * Created by yamlee on 7/8/16.
 */
class ReleasePreparer {
    private static final Logger LOG = Logging.getLogger(ReleasePreparer.class);

    /**
     * apk build number
     */
    public static final String KEY_VERSION_CODE = "VERSION_CODE"
    /**
     * apk version last patch name
     */
    public static final String KEY_VERSION_NAME = "VERSION_NAME_PATCH"
    /**
     * the last git commit SHA id
     */
    public static final String KEY_LAST_COMMIT_RECORD = "LAST_COMMIT_RECORD"

    public static enum VersionNameType {
        MAJOR("major"),
        MINOR("minor"),
        PATCH("patch")

        private String value

        VersionNameType(String value) {
            this.value = value
        }
    }

    VcsOperator vcsOperator
    Android androidProxy
    Project project

    ReleasePreparer(Project project, VcsOperator vcsOperator, Android android) {
        this.vcsOperator = vcsOperator
        this.androidProxy = android
        this.project = project
    }

    def run(String logIdentifyTag, VersionNameType type) {
        String filePath = Constants.releaseFilePath(project)
        if (createVersionPropertiesFileIfNotExist(filePath)) {
            versionCodeAdd(filePath, type)
            generateChangeLog(androidProxy.apkVersionName, logIdentifyTag)
        }
    }

    def generateChangeLog(String version, String logIdentifyTag) {
        String propertyFilePath = Constants.releaseFilePath(project)
        String changelogFilePath = Constants.changeLogFilePath(project)
        FileInputStream fileInputStream = new FileInputStream(propertyFilePath)
        Properties properties = new Properties()
        properties.load(fileInputStream)
        String lastReleaseCommitId = properties.getProperty(KEY_LAST_COMMIT_RECORD)

        LogMessage newestHistory = vcsOperator.log(1).get(0)
        String newestReleaseCommitId = newestHistory.id
        List<LogMessage> history
        if (lastReleaseCommitId == null || lastReleaseCommitId.equals("")) {
            history = vcsOperator.logAll()
        } else {
            history = vcsOperator.log(lastReleaseCommitId, newestReleaseCommitId)
        }
        if (history == null || history.size() == 0) {
            LOG.lifecycle("Vcs no commit history,abort auto generate change log")
            return;
        }
        LOG.lifecycle("Get VCS log history size ${history.size()}")
        def index = 1
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Change Log for ${version}:")
        StringBuilder logItemString = new StringBuilder()
        history.each { commit ->
            if (commit != null && commit.message != null && commit.message.startsWith(logIdentifyTag)) {
                logItemString.append("\n")
                def log = "${index}. ${commit.message.replace(logIdentifyTag, "")}"
                logItemString.append(log)
                index++
            }
        }
        if (logItemString.toString() == null || logItemString.toString() == "") {
            LOG.lifecycle("Vcs commit history not have any marked log since last release,now aborting add new change log")
            return
        }
        stringBuilder.append(logItemString.toString())
        stringBuilder.append("\n")
        println(stringBuilder.toString())
        writeChangeLog(changelogFilePath, stringBuilder)
    }

    def writeChangeLog(String changelogFilePath, StringBuilder stringBuilder) {
        try {
            File file = new File(changelogFilePath)
            if (!file.exists() && !file.isDirectory()) {
                file.createNewFile()
            }
            FileWriter fileWriter = new FileWriter(file, true)
            fileWriter.append(stringBuilder.toString())
            fileWriter.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }


    void versionCodeAdd(String filePath, VersionNameType versionNameType) {
        Properties properties = new Properties()
        FileInputStream fileInputStream = new FileInputStream(filePath)
        properties.load(fileInputStream)
        def versionCode = properties.getProperty(KEY_VERSION_CODE)
        def newVersionCode = Integer.parseInt(versionCode) + 1

        String versionName = properties.getProperty(KEY_VERSION_NAME)
        String[] versionNameArray = versionName.split('\\.')
        if (versionNameType == VersionNameType.MAJOR) {
            int majorNum = Integer.parseInt(versionNameArray[0]) + 1
            versionNameArray[0] = majorNum + ""
        } else if (versionNameType == VersionNameType.MINOR) {
            int minorNum = Integer.parseInt(versionNameArray[1]) + 1
            versionNameArray[1] = minorNum + ""
        } else if (versionNameType == VersionNameType.PATCH) {
            int patchNum = Integer.parseInt(versionNameArray[2]) + 1
            versionNameArray[2] = patchNum + ""
        }
        def newVersionName = formatVersionName(versionNameArray)
        properties.setProperty(KEY_VERSION_CODE, String.valueOf(newVersionCode))
        properties.setProperty(KEY_VERSION_NAME, String.valueOf(newVersionName))
        project.extensions.ext.versionCode = newVersionCode
        project.extensions.ext.versionName = newVersionName
        FileOutputStream fileOutputStream = new FileOutputStream(filePath)
        properties.store(fileOutputStream, "Modify new version")
        fileInputStream.close()
        fileOutputStream.close()
    }

    def formatVersionName(String[] versionNameArray) {
        String major = versionNameArray[0]
        String minor = versionNameArray[1]
        String patch = versionNameArray[2]
        return "${major}.${minor}.${patch}"
    }

    boolean createVersionPropertiesFileIfNotExist(String filePath) {
        try {
            File file = new File(filePath)
            if (!file.exists()) {
                LOG.lifecycle("release properties file not exist,now creating...")
                boolean isCreateSuccess = file.createNewFile()
                if (isCreateSuccess) {
                    LOG.lifecycle("release properties file create success")
                } else {
                    LOG.lifecycle("release properties file create fail,now stop execution")
                    throw new StopExecutionException("version properties file create fail")
                }
                Properties properties = new Properties()
                FileInputStream fileInputStream = new FileInputStream(file)
                properties.load(fileInputStream)
                properties.setProperty(KEY_VERSION_CODE, '1')
                properties.setProperty(KEY_VERSION_NAME, '1.0.0')
                FileOutputStream fos = new FileOutputStream(file)
                properties.store(fos, "Create new release properties file")
                fos.close()
                fileInputStream.close()
            }

        } catch (IOException e) {
            e.printStackTrace()
            return false
        }
        return true
    }

    def prepareApkVersionInfo() {
        LOG.lifecycle("apk release plugin:preparing for apk version info ")
        createVersionPropertiesFileIfNotExist(Constants.releaseFilePath(project))
        File file = new File(Constants.releaseFilePath(project))
        Properties properties = new Properties()
        file.withInputStream { inputStream ->
            properties.load(inputStream)
        }
        project.extensions.ext.versionCode = Integer.parseInt(properties.getProperty(KEY_VERSION_CODE))
        project.extensions.ext.versionName = properties.getProperty(KEY_VERSION_NAME)
    }
}
