package me.yamlee.apkrelease

import me.yamlee.apkrelease.internel.extension.ReleaseTarget
import org.gradle.api.NamedDomainObjectContainer

/**
 * Build extension property used in build.gradle file
 * Created by yamlee on 6/15/16.
 */
class ApkReleaseExtension {
    final private NamedDomainObjectContainer<ReleaseTarget> distributeTargets
    String apkPath
    String logIdentifyTag
    String versionType

    ApkReleaseExtension(NamedDomainObjectContainer<ReleaseTarget> distributeTargets) {
        this.distributeTargets = distributeTargets
    }

    public apkDistribute(Closure closure) {
        distributeTargets.configure(closure)
    }
}
