package me.yamlee.apkrelease.internel.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by yamlee on 6/24/16.
 */
class ApkRenameTask extends DefaultTask {
    @TaskAction
    def runTask() {
        println("rename apk file task running")
        //重命名apk包
        project.android.applicationVariants.all { variant ->
            //check if staging variant
            println "--------------------------------" + variant.name
            def defaultConfig = project.android.defaultConfig
            variant.outputs.each { output ->
                File file = output.outputFile
                String fileName = "Near_Merchant_v" + defaultConfig.versionName + "_" + variant.name + "_build" + defaultConfig.versionCode + ".apk"
                File newApkFile = new File(file.parent, fileName)
                output.outputFile = newApkFile
                project.extensions.ext.apkFilePath = newApkFile.absolutePath
            }
        }
    }


}
