package me.yamlee.apkrelease.internel.task

import me.yamlee.apkrelease.ReleaseJobManager
import me.yamlee.apkrelease.internel.iml.ApkFileResolverIml
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

    ApkReleaseTask() {


    }

    @TaskAction
    def runTask() {
        LOG.lifecycle("--------Apk Distribute task begin-------")
        ReleaseJobManager manager = new ReleaseJobManager(project, new ApkFileResolverIml())
        manager.run(buildFlavorName)
        LOG.lifecycle("--------Apk Distribute task end-------")
    }
}
