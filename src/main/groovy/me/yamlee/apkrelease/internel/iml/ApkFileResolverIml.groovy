package me.yamlee.apkrelease.internel.iml

import groovy.io.FileType
import me.yamlee.apkrelease.internel.ApkFileResolver
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created by yamlee on 7/6/16.
 */
class ApkFileResolverIml implements ApkFileResolver {

    @Override
    List<File> getApkFiles(Project project) {
        List<File> apkFileList = new ArrayList<>()
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
                    if (file.absolutePath.endsWith(".apk")) {
                        apkFileList.add(file)
                    }
                }
            }
        } else {
            apkFileList.add(apkFile)
        }
        if (apkFileList.size() == 0) {
            throw new GradleException("could not find apk file")
        }
        return apkFileList
    }
}
