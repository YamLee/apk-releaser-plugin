package me.yamlee.apkrelease

import me.yamlee.apkrelease.internel.ApkFileResolver
import me.yamlee.apkrelease.internel.VcsAutoCommitor
import me.yamlee.apkrelease.internel.iml.AndroidProxy
import me.yamlee.apkrelease.internel.vcs.GitVcsOperator
import org.apache.commons.lang.WordUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

/**
 * Created by yamlee on 7/7/16.
 * Manage all release job ,including upload apk job ,vcs msg commit job
 */
class ReleaseJobManager {
    static final Logger LOG = Logging.getLogger(ReleaseJobManager)
    Project project
    ApkFileResolver apkFileResolver

    ReleaseJobManager(Project project, ApkFileResolver apkFileResolver) {
        this.project = project
        this.apkFileResolver = apkFileResolver
    }

    def void run(String buildFlavorName) {
        String formatName = WordUtils.capitalize(buildFlavorName)
        def buildFavorTargetTask = project.tasks.findByName("assemble${formatName}")
        if (buildFavorTargetTask == null) {
            throw new GradleException("android task \"assemble$formatName\" not found")
        }

        //1.Find target apk file
        File apkFile = null
        List<File> apkFiles = apkFileResolver.getApkFiles(project)
        apkFiles.each { File file ->
            if (file.getName().contains(buildFlavorName)) {
                apkFile = file
            }
        }
        if (apkFile == null) {
            throw new GradleException("Can not upload apk file to pgyer because can not find target apk file")
        }

        //2.Upload apk file to pgyer
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
            }
        }

        //3.Commit msg to version control system
        VcsAutoCommitor vcsAutoCommitor = new VcsAutoCommitor(project, new GitVcsOperator(), new AndroidProxy(project))
        vcsAutoCommitor.commitMsgToVcs()
    }
}
