package me.yamlee.apkrelease.internel.iml

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.internel.Android
import org.gradle.api.Project

/**
 * Created by yamlee on 7/8/16.
 */
class AndroidProxy implements Android{
    private Project project
    def config

    AndroidProxy(Project project) {
        this.project = project
        config = project.android.getProperty('defaultConfig')
    }

    @Override
    String getApkVersionName() {
        return config.versionName + Constants.FILE_CONNECTOR + config.versionCode
    }

    @Override
    long getVersionCode() {
        return config.versionCode
    }

    @Override
    String getVersionName() {
        return config.versionName
    }
}
