package me.yamlee.apkrelease.internel

import com.android.build.gradle.AppPlugin
import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project

/**
 * Auto commit message to version control system
 * Created by yamlee on 6/16/16.
 */
class VcsAutoCommitor {

    public static final String VERSION_CODE_KEY = "VERSION_CODE"
    public static final String VERSION_NAME_PATCH_KEY = "VERSION_NAME_PATCH"

    def commitBuildMsg(Project project) {
        String filePath = project.getRootDir() + File.separator + "release.properties"
        if (createVersionPropertiesFileIfNotExist(filePath)) {
            versionCodeAdd(filePath)
        }

    }


    def commitToVcs(Project project) {
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
        def config = getAndroidConfig(project)
        def commitMsg = "Build version for " + config.versionName + "_" + config.versionCode
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

    def generateChangeLog() {
        Grgit git = Grgit.open()
        def history = git.log {
            range '00bb7fd', '003bee9'
        }
//        def history = git.log()
        history.each { commit ->
            println commit.shortMessage
        }
    }


    def getAndroidConfig(Project project) {
        return project.android.getProperty('defaultConfig')
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
