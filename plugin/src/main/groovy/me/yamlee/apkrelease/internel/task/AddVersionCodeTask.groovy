package me.yamlee.apkrelease.internel.task

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.internel.ReleasePreparer
import me.yamlee.apkrelease.internel.iml.AndroidProxy
import me.yamlee.apkrelease.internel.vcs.GitVcsOperator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by yamlee on 8/1/16.
 */
class AddVersionCodeTask extends DefaultTask {
    ReleasePreparer.VersionNameType versionNameType;
    @TaskAction
    def runTask() {
        ReleasePreparer releasePreparer = new ReleasePreparer(project,
                new GitVcsOperator(), new AndroidProxy(project))
        releasePreparer.setAddedVersionCode(Constants.releaseFilePath(project),
                versionNameType)
    }
}
