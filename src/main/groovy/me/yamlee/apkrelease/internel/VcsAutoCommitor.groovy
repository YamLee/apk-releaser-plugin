package me.yamlee.apkrelease.internel

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.internel.vcs.LogMessage
import me.yamlee.apkrelease.internel.vcs.VcsOperator
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

/**
 * Auto commit message to version control system
 * Created by yamlee on 6/16/16.
 */
class VcsAutoCommitor {
    private static final Logger LOG = Logging.getLogger(VcsAutoCommitor.class);

    /**
     * apk build number
     */
    public static final String VERSION_CODE_KEY = "VERSION_CODE"
    /**
     * apk version last patch name
     */
    public static final String VERSION_NAME_PATCH_KEY = "VERSION_NAME_PATCH"
    /**
     * the last git commit SHA id
     */
    public static final String LAST_COMMIT_RECORD_KEY = "LAST_COMMIT_RECORD"

    VcsOperator vcsOperator
    Android androidProxy
    Project project

    VcsAutoCommitor(Project project, VcsOperator vcsOperator, Android android) {
        this.vcsOperator = vcsOperator
        this.androidProxy = android
        this.project = project
    }

    def run(String logIdentifyTag) {
        String filePath = Constants.releaseFilePath(project)
        if (createVersionPropertiesFileIfNotExist(filePath)) {
            versionCodeAdd(filePath)
            commitMsgToVcs(logIdentifyTag)
        }
    }


    def commitMsgToVcs(String logIdentifyTag) {
        String version = androidProxy.apkVersionName
        def commitMsg = "Build version for " + version
        generateChangeLog(version, logIdentifyTag)
        List<String> branchNames = vcsOperator.branchList()
        if (branchNames.contains("ci_branch")) {
            vcsOperator.checkOut("ci_branch", false)
        } else {
            vcsOperator.checkOut("ci_branch", true)
        }
        vcsOperator.commit(commitMsg)

        String lastCommitMsg = vcsOperator.log(1).get(0).message
        vcsOperator.addTag("v${androidProxy.versionName}_${androidProxy.versionCode}", lastCommitMsg)
        try {
            vcsOperator.push()
            vcsOperator.pushTags()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def generateChangeLog(String version, String logIdentifyTag) {
        String propertyFilePath = Constants.releaseFilePath(project)
        String changelogFilePath = Constants.changeLogFilePath(project)
        FileInputStream fileInputStream = new FileInputStream(propertyFilePath)
        Properties properties = new Properties()
        properties.load(fileInputStream)
        String lastReleaseCommitId = properties.getProperty(LAST_COMMIT_RECORD_KEY)

        LogMessage newestHistory = vcsOperator.log(1).get(0)
        String newestReleaseCommitId = newestHistory.id
        def history
        if (lastReleaseCommitId == null || lastReleaseCommitId.equals("")) {
            history = vcsOperator.logAll()
        } else {
            history = vcsOperator.log(lastReleaseCommitId, newestReleaseCommitId)
        }
        def index = 1
        StringBuilder stringBuilder = new StringBuilder()
        stringBuilder.append("Change Log for ${version}:")
        history.each { commit ->
            if (commit.message.startsWith(logIdentifyTag)) {
                stringBuilder.append("\n")
                def log = "${index}. ${commit.message.replace(logIdentifyTag, "")}"
                stringBuilder.append(log)
            }
            index++
        }
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


    void versionCodeAdd(String filePath) {
        Properties properties = new Properties()
        FileInputStream fileInputStream = new FileInputStream(filePath)
        properties.load(fileInputStream)
        def versionCode = properties.getProperty(VERSION_CODE_KEY)
        def patchVersionName = properties.getProperty(VERSION_NAME_PATCH_KEY)
        def newVersionCode = Integer.parseInt(versionCode) + 1
        def newPatchVersion = Integer.parseInt(patchVersionName) + 1
        properties.setProperty(VERSION_CODE_KEY, String.valueOf(newVersionCode))
        properties.setProperty(VERSION_NAME_PATCH_KEY, String.valueOf(newPatchVersion))
        FileOutputStream fileOutputStream = new FileOutputStream(filePath)
        properties.store(fileOutputStream, "Modify new version")
        fileInputStream.close()
        fileOutputStream.close()
    }

    boolean createVersionPropertiesFileIfNotExist(String filePath) {
        try {
            File file = new File(filePath)
            if (!file.exists()) {
                LOG.info("version properties file not exist,now creating...")
                boolean isCreateSuccess = file.createNewFile()
                if (isCreateSuccess) {
                    LOG.info("version properties file create success")
                } else {
                    LOG.info("version properties file create fail,now stop execution")
                    throw new StopExecutionException("version properties file create fail")
                }
            }
            Properties properties = new Properties()
            FileInputStream fileInputStream = new FileInputStream(file)
            properties.load(fileInputStream)
            properties.setProperty(VERSION_CODE_KEY, '1')
            properties.setProperty(VERSION_NAME_PATCH_KEY, '0')
            FileOutputStream fos = new FileOutputStream(file)
            properties.store(fos, "Create new version properties file")
            fos.close()
            fileInputStream.close()
        } catch (IOException e) {
            e.printStackTrace()
            return false
        }
        return true
    }

}
