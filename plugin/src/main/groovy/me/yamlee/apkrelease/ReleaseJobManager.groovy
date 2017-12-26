package me.yamlee.apkrelease

import me.yamlee.apkrelease.internel.ApkFileResolver
import me.yamlee.apkrelease.internel.ReleasePreparer
import me.yamlee.apkrelease.internel.VcsAutoCommitor
import me.yamlee.apkrelease.internel.iml.AndroidProxy
import me.yamlee.apkrelease.internel.vcs.GitVcsOperator
import me.yamlee.apkrelease.internel.vcs.VcsOperator
import org.apache.commons.lang.WordUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

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

        //0. Generate change log
        VcsOperator vcsOperator = new GitVcsOperator()
        AndroidProxy androidProxy = new AndroidProxy(project)
        ReleasePreparer releasePreparer = new ReleasePreparer(project, vcsOperator, androidProxy)
        String logIdentifyTag = project.extensions.apkRelease.logIdentifyTag
        if (null == logIdentifyTag || logIdentifyTag.equals("")) {
            logIdentifyTag = "*"
        }
        releasePreparer.run(logIdentifyTag, buildFlavorName)

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


        def items = project.apkRelease.distributeTargets
        LOG.lifecycle("...distribute iterate flavors....")
        LOG.lifecycle("...flavors size is:${items.size()}....")
        for (target in items) {
            String targetName = target.name
            if (targetName.equalsIgnoreCase(buildFlavorName)) {
                //2.Upload apk file to pgyer
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

                //3.Commit msg to version control system
                if (target.autoCommitToCVS) {
                    LOG.lifecycle("...autoCommitToCVS enabled ,now auto commit msg and create target tag,then push to remote")
                    VcsAutoCommitor vcsAutoCommitor = new VcsAutoCommitor(project, new GitVcsOperator(), new AndroidProxy(project))
                    vcsAutoCommitor.commitMsgToVcs()
                } else {
                    LOG.lifecycle("...autoCommitToCVS disabled ,now all child task finished")
                }
            }
        }


    }
}
