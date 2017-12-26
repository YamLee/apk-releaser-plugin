package me.yamlee.apkrelease

import org.gradle.api.Project;

/**
 * Created by yamlee on 7/9/16.
 * Global properties which can used by external build script
 */
class Global {
    private Project project

    public static Global get(Project project) {
        return new Global(project)
    }

    private Global(Project project) {
        this.project = project
    }

    public void setApkFilePath(String apkFilePath) {
        project.extensions.ext.apkFilePath = apkFilePath
    }

    public String getApkFilePath() {
        return project.extensions.ext.apkFilePath
    }

    public void setVersionCode(int versionCode) {
        project.extensions.ext.versionCode = versionCode
    }

    public int getVersionCode() {
        return project.extensions.ext.versionCode
    }

    public void setVersionName(String versionName) {
        project.extensions.ext.versionName = versionName
    }

    public String getVersionName() {
        return project.extensions.ext.versionName
    }

}
