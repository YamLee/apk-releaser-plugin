package me.yamlee.apkrelease.internel

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project

/**
 * Auto commit message to version control system
 * Created by yamlee on 6/16/16.
 */
class VcsAutoCommitor {

    public static final String VERSION_CODE_KEY = "VERSION_CODE"
    public static final String VERSION_NAME_PATCH_KEY = "VERSION_NAME_PATCH"
    public static final String LAST_COMMIT_RECORD_KEY = "LAST_COMMIT_RECORD"

    def run(Project project) {
        String filePath = project.getRootDir() + File.separator + "release.properties"
        String logFilePath = project.getRootDir() + File.separator + "changelog.md"
        if (createVersionPropertiesFileIfNotExist(filePath)) {
            versionCodeAdd(filePath)
            commitMsgToVcs(project)
        }
    }


    def commitMsgToVcs(Project project) {
        String version = getApkVersion(project)
        def commitMsg = "Build version for " + version

        String propertyPath = project.getRootDir() + File.separator + "release.properties"
        String logFilePath = project.getRootDir() + File.separator + "changelog.md"
        generateChangeLog(logFilePath, propertyPath, version)

        Grgit git = Grgit.open()
        def branches = git.branch.list()
        List<String> branchNames = new ArrayList<>()
        branches.each {
            println it.name
            branchNames.add(it.name)
        }
        if (branchNames.contains("ci_branch")) {
            git.checkout(branch: 'ci_branch', createBranch: false)
        } else {
            git.checkout(branch: 'ci_branch', createBranch: true)
        }
        git.commit(message: commitMsg, all: true)
        def history = git.log(maxCommits: 1)
        git.tag.add(name: 'v' + config.versionName + "_" + config.versionCode, message: history.get(0).shortMessage)
        try {
            git.push(all: true)
            git.push(tags: true)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def String getApkVersion(Project project) {
        def config = project.android.getProperty('defaultConfig')
        return config.versionName + "_" + config.versionCode
    }

    def generateChangeLog(String changelogFilePath, String propertyFilePath, String version) {
        FileInputStream fileInputStream = new FileInputStream(propertyFilePath)
        Properties properties = new Properties()
        properties.load(fileInputStream)
        String lastReleaseCommitId = properties.getProperty(LAST_COMMIT_RECORD_KEY)

        Grgit git = Grgit.open()
        def newestHistory = git.log(maxCommits: 1)
        String newestReleaseCommitId = newestHistory.id
        def history
        if (lastReleaseCommitId == null || lastReleaseCommitId.equals("")) {
            history = git.log()
        } else {
            history = git.log {
                range lastReleaseCommitId, newestReleaseCommitId
            }
        }
        def index = 1
        StringBuilder stringBuilder = new StringBuilder(version)
        history.each { commit ->
            def log = "${index}. ${commit.shortMessage}"
            println log
            if (log.startsWith("*")) {
                stringBuilder.append("\n")
                stringBuilder.append(log)
            }
            index++
        }
        writeChangeLog(changelogFilePath, stringBuilder)
    }

    def writeChangeLog(String changelogFilePath, StringBuilder stringBuilder) {
        try {
            File file = new File(changelogFilePath)
            if (!file.exists() && !file.isDirectory()) {
                file.createNewFile()
            }
            FileWriter fileWriter = new FileWriter(file, true)
            fileWriter.write("\n")
            fileWriter.write(stringBuilder.toString())
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
                file.createNewFile()
            }
            Properties properties = new Properties()
            FileInputStream fileInputStream = new FileInputStream(file)
            properties.load(fileInputStream)
            properties.setProperty(VERSION_CODE_KEY, '1')
            properties.setProperty(VERSION_NAME_PATCH_KEY, '1')
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
