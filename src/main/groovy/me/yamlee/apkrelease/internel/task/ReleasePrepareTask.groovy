package me.yamlee.apkrelease.internel.task

import me.yamlee.apkrelease.internel.ReleasePreparer
import me.yamlee.apkrelease.internel.iml.AndroidProxy
import me.yamlee.apkrelease.internel.vcs.GitVcsOperator
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

/**
 * Created by yamlee on 6/21/16.
 */
class ReleasePrepareTask extends DefaultTask {
    private static final Logger LOG = Logging.getLogger(ReleasePrepareTask.class);
    String logIdentifyTag = "*"
    String versionNameAddType = "patch"

    @TaskAction
    def runTask() {
        LOG.lifecycle("--------Prepare task begin--------")
        ReleasePreparer preparer = new ReleasePreparer(project, new GitVcsOperator(), new AndroidProxy(project))
        ReleasePreparer.VersionNameType versionNameType
        if (versionNameAddType == "major") {
            versionNameType = ReleasePreparer.VersionNameType.MAJOR
        } else if (versionNameAddType == "minor") {
            versionNameType = ReleasePreparer.VersionNameType.MINOR
        } else {
            versionNameType = ReleasePreparer.VersionNameType.PATCH
        }
        preparer.run(logIdentifyTag, versionNameType)

        LOG.lifecycle("--------Prepare task end--------")
    }
}
