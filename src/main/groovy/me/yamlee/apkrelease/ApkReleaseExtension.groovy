package me.yamlee.apkrelease

import me.yamlee.apkrelease.internel.ReleaseTarget
import org.gradle.api.NamedDomainObjectContainer

/**
 * Build extension property used in build.gradle file
 * Created by yamlee on 6/15/16.
 */
class ApkReleaseExtension {
    final private NamedDomainObjectContainer<ReleaseTarget> releaseTargets

    ApkReleaseExtension(NamedDomainObjectContainer<ReleaseTarget> releaseTargets) {
        this.releaseTargets = releaseTargets
    }

    public item(Closure closure) {
        releaseTargets.configure(closure)
    }
}
