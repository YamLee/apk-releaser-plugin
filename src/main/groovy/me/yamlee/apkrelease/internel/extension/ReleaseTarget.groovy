package me.yamlee.apkrelease.internel.extension

import org.gradle.api.Named
import org.gradle.api.internal.project.ProjectInternal

/**
 * Created by yamlee on 6/21/16.
 */
class ReleaseTarget implements Named {
    String name
    ProjectInternal target
    /**
     * third party api key
     */
    String pgyerApiKey
    /**
     * third party user key
     */
    String pgyerUserKey
    /**
     * give apk file name template when rename generated apk file
     */
    String apkFileNameTemplate

    ReleaseTarget(String name) {
        this.name = name
        this.target = target
    }

    @Override
    String getName() {
        return name
    }
}
