package me.yamlee.apkrelease

import me.yamlee.apkrelease.internel.task.ApkReleaseTask
import me.yamlee.apkrelease.internel.extension.ReleaseTarget
import me.yamlee.apkrelease.internel.task.ChannelPackageTask
import me.yamlee.apkrelease.internel.task.GitCommitTask
import org.apache.commons.lang.WordUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 *
 * Plugin Main class
 */
class ApkReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def item = project.container(ReleaseTarget) { buildFlavorName ->
            String formatName = WordUtils.capitalize(buildFlavorName.toString())
            def releaseTask = project.task("apkRelease${formatName}", type: ApkReleaseTask)
            releaseTask.group = 'apkRelease'
            releaseTask.description = 'release apk with auto commit msg to git and upload apk to pgyer'
            releaseTask.buildFlavorName = buildFlavorName

            def channelPackageTask = project.task("packageChannelApkFrom${formatName}", type: ChannelPackageTask, dependsOn: "assemble${formatName}")
            channelPackageTask.group = 'apkRelease'
            channelPackageTask.description = 'build apk file with different channel'
            channelPackageTask.buildFlavorName = buildFlavorName

            def gitCommitTask = project.task("gitAutoCommit", type: GitCommitTask)
            gitCommitTask.group = 'apkRelease'
            gitCommitTask.description = 'generate changelog and add build code ,then commit to git and create a tag'

            project.extensions.create(buildFlavorName, ReleaseTarget, formatName)
        }
        def apkReleaseExtension = new ApkReleaseExtension(item)
        project.extensions.apkRelease = apkReleaseExtension
//        project.extensions.create("apkRelease", ApkReleaseExtension)
    }

}