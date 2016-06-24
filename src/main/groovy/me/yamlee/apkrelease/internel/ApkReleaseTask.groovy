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
        def items = project.apkRelease.distributeTargets
        for (target in items) {
            String targetName = target.name
            if (targetName.equalsIgnoreCase(buildFlavorName)) {
                String pgyerApiKey = target.pgyerApiKey
                String pgyerUserKey = target.pgyerUserKey
                LOG.info("$buildFlavorName pgyerApiKey is ${pgyerApiKey}")
                println("$buildFlavorName pgyerApiKey is $pgyerApiKey")
                LOG.info("$buildFlavorName pgyerUserKey is $pgyerUserKey")
                println("$buildFlavorName pgyerUserKey is $pgyerUserKey")
                project.apply plugin: 'org.quanqi.pgyer'
                project.pgyer {
                    _api_key = pgyerApiKey
                    uKey = pgyerUserKey
                }

            }
        }

    }
}
