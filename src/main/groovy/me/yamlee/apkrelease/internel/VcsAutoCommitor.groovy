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
            generateChangeLog(androidProxy.apkVersionName, logIdentifyTag)
            commitMsgToVcs()
        }
    }


    def commitMsgToVcs() {
        String version = androidProxy.apkVersionName
        def commitMsg = "Build version for " + version
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



}
