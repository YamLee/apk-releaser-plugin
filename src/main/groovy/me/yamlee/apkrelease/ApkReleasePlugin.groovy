package me.yamlee.apkrelease

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 *
 * Plugin Main class
 */
class ApkReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("apkRelease", ApkReleaseExtension)
        project.task('hello') << {
            println("Hello ${project.apkRelease.message}")
        }

    }

}