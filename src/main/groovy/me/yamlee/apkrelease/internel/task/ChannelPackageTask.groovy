package me.yamlee.apkrelease.internel.task

import me.yamlee.apkrelease.internel.ApkFileResolver
import me.yamlee.apkrelease.internel.ChannelApkGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

/**
 * Created by yamlee on 6/21/16.
 */
class ChannelPackageTask extends DefaultTask {
    private static final Logger log = Logging.getLogger(ChannelPackageTask.class);
    String buildFlavorName



    @TaskAction
    def runTask() {
        log.info("run channel package task")
        List<File> apkFiles = ApkFileResolver.getApkFiles(project)
        File targetFile = null
        apkFiles.each { File file ->
            if (file.absolutePath.equalsIgnoreCase(buildFlavorName)) {
                targetFile = file
            }
        }
        ChannelApkGenerator generator = new ChannelApkGenerator(targetFile.absolutePath)
        generator.execute(project)
    }
}
