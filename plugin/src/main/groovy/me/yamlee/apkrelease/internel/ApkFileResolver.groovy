package me.yamlee.apkrelease.internel

import org.gradle.api.Project

/**
 * Created by yamlee on 7/7/16.
 */
interface ApkFileResolver {
    /**
     * get apk file path list
     * @return
     */
    List<File> getApkFiles(Project project)
}