package me.yamlee.apkrelease.internel

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

/**
 * Created by yamlee on 6/21/16.
 */
class ApkReleaseTask extends DefaultTask {
    private static final Logger LOG = Logging.getLogger(ApkReleaseTask.class);
    String buildFlavorName

    @TaskAction
    def runTask() {
        def items = project.apkRelease.releaseTargets
        for (target in items) {
            String pgyerApiKey = target.pgyerApiKey
            String pgyerUserKey = target.pgyerUserKey
            LOG.info("$buildFlavorName pgyerApiKey is $pgyerApiKey")
            LOG.info("$buildFlavorName pgyerUserKey is $pgyerUserKey")
        }

    }
}
