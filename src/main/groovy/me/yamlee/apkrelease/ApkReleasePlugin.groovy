package me.yamlee.apkrelease

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import me.yamlee.apkrelease.internel.task.ApkReleaseTask
import me.yamlee.apkrelease.internel.extension.ReleaseTarget
import me.yamlee.apkrelease.internel.task.ChannelPackageTask
import me.yamlee.apkrelease.internel.task.GitCommitTask
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

        log.info("rename apk file configure")
        //rename apk file name
        project.android.applicationVariants.all { variant ->
            //check if staging variant
            println "android variant name ---> $variant.name"
            def defaultConfig = project.android.defaultConfig
            variant.outputs.each { output ->
                File file = output.outputFile
                String fileName = "Near_Merchant_v${defaultConfig.versionName}_${variant.name}_build${defaultConfig.versionCode}.apk"
                log.info("renamed apk file is $fileName")
                File newApkFile = new File(file.parent, fileName)
                output.outputFile = newApkFile
                project.extensions.ext.apkFilePath = newApkFile.absolutePath
            }
        }

        def item = project.container(ReleaseTarget) { buildFlavorName ->
            String formatName = WordUtils.capitalize(buildFlavorName.toString())
            def releaseTask = project.task("apkDist${formatName}",
                    type: ApkReleaseTask,
                    dependsOn: "assemble${formatName}")
            releaseTask.group = 'apkRelease'
            releaseTask.description = 'release apk with auto commit msg to git and upload apk to pgyer'
            releaseTask.buildFlavorName = buildFlavorName

            def channelPackageTask = project.task("channelApkFrom${formatName}",
                    type: ChannelPackageTask,
                    dependsOn: "assemble${formatName}")
            channelPackageTask.group = 'apkRelease'
            channelPackageTask.description = 'build apk file with different channel'
            channelPackageTask.buildFlavorName = buildFlavorName
            project.extensions.create(buildFlavorName, ReleaseTarget, formatName)
        }

        def gitCommitTask = project.task("gitAutoCommit", type: GitCommitTask)
        gitCommitTask.group = 'apkRelease'
        gitCommitTask.description = 'generate changelog and add build code ,then commit to git and create a tag'

        def apkReleaseExtension = new ApkReleaseExtension(item)
        project.extensions.apkRelease = apkReleaseExtension
//        project.extensions.create("apkRelease", ApkReleaseExtension)
    }


    static def hasAndroidPlugin(Project project) {
        return project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)
    }

    static def isOfflineBuild(Project project) {
        return project.getGradle().getStartParameter().isOffline()
    }

}