package me.yamlee.apkrelease.internel.task

import groovy.io.FileType
import me.yamlee.apkrelease.internel.VcsAutoCommitor
import org.apache.commons.lang.WordUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
        String formatName = WordUtils.capitalize(buildFlavorName)
        def buildFavorTargetTask = project.tasks.findByName("assemble${formatName}")
        if (buildFavorTargetTask == null) {
            throw new GradleException("android task \"assemble$formatName\" not found")
        }

        //1. Rename apk file and this task depends on android assemble task
//        buildFavorTargetTask.getActions().get(0).execute(buildFavorTargetTask)
        def renameTask = project.task("renameApk", type: ApkRenameTask,dependsOn:"assemble${formatName}")
        renameTask.dependsOn buildFavorTargetTask
        renameTask.execute()
//        renameTask.getActions().get(0).execute(renameTask)

        String apkFilePath
        if (project.extensions.extraProperties.has("apkFilePath")) {
            apkFilePath = project.extensions.ext.apkFilePath
        }
        File apkFile
        if (apkFilePath != null && apkFilePath != "") {
            apkFile = new File(apkFilePath)
        }
        if (apkFile == null || !apkFile.exists()) {
            File apkFileDir = project.file("$project.buildDir/outputs/apk")
            if (apkFileDir != null && apkFileDir.exists()) {
                apkFileDir.eachFileRecurse(FileType.FILES) { file ->
                    if (file.absolutePath.contains("build")) {
                        apkFile = file
                    }
                }
            }
        }
        if (apkFile == null || !apkFile.exists()) {
            throw new GradleException("could not find apk file")
        }
        //3.Upload apk file to pgyer
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
                project.apks {
                    distribute {
                        sourceFile = apkFile
                    }
                }
                def uploadTask = project.tasks.findByName("uploadPgyer")
                uploadTask.execute()
//                uploadTask.getActions().get(0).execute(uploadTask)
            }
        }
        //3.Commit msg to version control system
//        VcsAutoCommitor vcsAutoCommitor = new VcsAutoCommitor()
//        vcsAutoCommitor.run(project)
    }
}