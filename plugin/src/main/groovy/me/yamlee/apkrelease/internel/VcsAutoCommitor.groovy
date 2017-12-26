package me.yamlee.apkrelease.internel

import me.yamlee.apkrelease.internel.vcs.VcsOperator
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Auto commit message to version control system
 * Created by yamlee on 6/16/16.
 */
class VcsAutoCommitor{
    private static final Logger LOG = Logging.getLogger(VcsAutoCommitor.class);

    VcsOperator vcsOperator
    Android androidProxy
    Project project

    VcsAutoCommitor(Project project, VcsOperator vcsOperator, Android android) {
        this.vcsOperator = vcsOperator
        this.androidProxy = android
        this.project = project
    }

    def commitMsgToVcs() {
        String version = androidProxy.apkVersionName
        def commitMsg = "Build version for " + version
        String branchName = project.extensions.apkRelease.branchName
        if (branchName == null || branchName.equals("")) {
            throw new GradleException("branchName can not be empty")
        }
        List<String> branchNames = vcsOperator.branchList()
        if (branchNames.contains(branchName)) {
            LOG.lifecycle("branch ${branchName} exsist,now check out...")
            vcsOperator.checkOut(branchName, false)
        } else {
            LOG.lifecycle("branch ${branchName} do not exsist,now create new...")
            vcsOperator.checkOut(branchName, true)
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
