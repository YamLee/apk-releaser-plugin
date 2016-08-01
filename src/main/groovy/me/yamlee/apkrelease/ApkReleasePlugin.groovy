package me.yamlee.apkrelease

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import me.yamlee.apkrelease.internel.ReleasePreparer
import me.yamlee.apkrelease.internel.extension.ReleaseTarget
import me.yamlee.apkrelease.internel.iml.AndroidProxy
import me.yamlee.apkrelease.internel.task.AddVersionCodeTask
import me.yamlee.apkrelease.internel.task.ApkReleaseTask
import me.yamlee.apkrelease.internel.task.ChannelPackageTask
import me.yamlee.apkrelease.internel.vcs.GitVcsOperator
import me.yamlee.apkrelease.internel.vcs.VcsOperator
import org.apache.commons.lang.WordUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.StopExecutionException

/**
 *
 * Plugin Main class
 */
class ApkReleasePlugin implements Plugin<Project> {
    private static final Logger log = Logging.getLogger ApkReleasePlugin

    @Override
    void apply(Project project) {
        if (!hasAndroidPlugin(project)) {
            throw new StopExecutionException(
                    "ApkRelease plugin must be applied after 'android' or 'android-library' plugin.")
        }

        def item = project.container(ReleaseTarget) { buildFlavorName ->
            String formatName = WordUtils.capitalize(buildFlavorName.toString())
            def releaseTask = project.task("apkDist${formatName}",
                    type: ApkReleaseTask,
                    dependsOn: ["assemble${formatName}"])
            releaseTask.group = 'apkRelease'
            releaseTask.description = 'release apk with auto commit msg to git and upload apk to pgyer'
            releaseTask.buildFlavorName = buildFlavorName

            //generate channel package depends on new build
            def channelPackageTask = project.task("channelFrom${formatName}WithNewBuild",
                    type: ChannelPackageTask,
                    dependsOn: "assemble${formatName}")
            channelPackageTask.group = 'apkRelease'
            channelPackageTask.description = 'build apk file with different channel'
            channelPackageTask.buildFlavorName = buildFlavorName

            //generate channel package without new build
            def channelTask = project.task("channelFrom${formatName}", type: ChannelPackageTask)
            channelTask.group = 'apkRelease'
            channelTask.description = 'build apk file with different channel'
            channelTask.buildFlavorName = buildFlavorName

            project.extensions.create(buildFlavorName, ReleaseTarget, formatName)
        }
        def apkReleaseExtension = new ApkReleaseExtension(item)
        project.extensions.apkRelease = apkReleaseExtension


        project.task("addPatchVersionName", type: AddVersionCodeTask, {
            versionNameType = ReleasePreparer.VersionNameType.PATCH
        })

        project.task("addMinorVersionName", type: AddVersionCodeTask, {
            versionNameType = ReleasePreparer.VersionNameType.MINOR
        })

        project.task("addMajorVersionName", type: AddVersionCodeTask, {
            versionNameType = ReleasePreparer.VersionNameType.MAJOR
        })

        VcsOperator vcsOperator = new GitVcsOperator()
        AndroidProxy androidProxy = new AndroidProxy(project)
        ReleasePreparer releasePreparer = new ReleasePreparer(project, vcsOperator, androidProxy)
        releasePreparer.prepareApkVersionInfo()

        log.lifecycle("rename apk file configure")
        Global global = Global.get(project)
        project.android.applicationVariants.all { variant ->
            //check if staging variant
            log.lifecycle "android variant name ---> $variant.name"
            def defaultConfig = project.android.defaultConfig
            variant.outputs.each { output ->
                File file = output.outputFile
                String fileName = "Near_Merchant_v${defaultConfig.versionName}_${variant.name}_build${defaultConfig.versionCode}.apk"
                log.lifecycle("renamed apk file is $fileName")
                File newApkFile = new File(file.parent, fileName)
                output.outputFile = newApkFile
                global.apkFilePath = newApkFile.absolutePath
            }
        }

    }


    static def hasAndroidPlugin(Project project) {
        return project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)
    }

    static def isOfflineBuild(Project project) {
        return project.getGradle().getStartParameter().isOffline()
    }

}