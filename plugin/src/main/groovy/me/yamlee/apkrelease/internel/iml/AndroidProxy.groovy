package me.yamlee.apkrelease.internel.iml

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.internel.Android
import org.gradle.api.Project
import org.gradle.api.tasks.StopExecutionException

/**
 * Created by yamlee on 7/8/16.
 */
class AndroidProxy implements Android{
    private Project project
    def config

    AndroidProxy(Project project) {
        this.project = project

    }

    def prepareDefaultConfig() {
        config = project.android.getProperty('defaultConfig')
        if (config == null) {
            throw new StopExecutionException("could not find android defaultConfig ")
        }
    }

    @Override
    String getApkVersionName() {
        prepareDefaultConfig()
        return config.versionName + Constants.FILE_CONNECTOR + config.versionCode
    }

    @Override
    int getVersionCode() {
        prepareDefaultConfig()
        return config.versionCode
    }

    @Override
    String getVersionName() {
        prepareDefaultConfig()
        return config.versionName
    }
}
