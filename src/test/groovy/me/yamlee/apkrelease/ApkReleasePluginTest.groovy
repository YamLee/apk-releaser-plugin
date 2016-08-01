package me.yamlee.apkrelease

import me.yamlee.apkrelease.util.FileCreator
import org.apache.commons.lang.WordUtils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.quanqi.pgyer.gradle.plugins.ApkTarget
import org.quanqi.pgyer.gradle.plugins.PgyerAllUploadTask
import org.quanqi.pgyer.gradle.plugins.PgyerExtension
import org.quanqi.pgyer.gradle.plugins.PgyerUserUploadTask

import static org.hamcrest.MatcherAssert.*


class ApkReleasePluginTest {
    Project project
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()
    File rootDir

    @Before
    public void setUp() throws Exception {
        rootDir = temporaryFolder.newFolder("project")
        project = ProjectBuilder.builder()
                .withProjectDir(rootDir)
                .build()
        project.apply plugin: 'com.android.application'
        project.apply plugin: ApkReleasePlugin

        project.android {
            def global = project.extensions.ext

            buildToolsVersion '23.0.2'

            //     <!-- 解决Android L上通知显示异常问题，targetSdkVersion需要设置成22 -->
            defaultConfig {
                applicationId "in.haojin.nearbymerchant"
                versionCode global.versionCode
                versionName global.versionName
                // Enabling multidex support.
                multiDexEnabled true

            }

            buildTypes {

                release {
                    minifyEnabled true
                    proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
                    debuggable false;
                    zipAlignEnabled true
                }

                debug {
                    minifyEnabled false
                    proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
                    debuggable true;
                }
            }

            defaultConfig {
                testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
            }

            productFlavors {

                haojin {
                    manifestPlaceholders = [channelName: "haojin", isFirstLauncher: "false"]
                }
            }
        }

        project.apkRelease{
            branchName = 'test_branch'
            logIdentifyTag = "*"
            versionType="patch"
        }

        project.apkRelease.apkDistribute {

            haojin {
                pgyerApiKey = "d850a2d47152c163ce9e58dc70ce3db6"
                pgyerUserKey = "5a1b726ce3904b2de445234f6fa4bb6c"
                generateChangeLog = false;
                autoCommitToCVS = false;
                autoAddVersionCode = true;
            }
        }
    }


    @Test
    public void testDistTask() throws Exception {
        def haojinTask = project.tasks.findByName("apkDistHaojin")
        assertThat(haojinTask, Matchers.notNullValue())

        Global global = Global.get(project)
        File apkFile = FileCreator.createApkFile(rootDir, "test_haojin.apk")
        global.apkFilePath = apkFile.absolutePath

        project.tasks.assembleHaojin << {
            println "assembleHaojin task run"
        }
        haojinTask.execute()
    }


    @Test
    public void testPrepare() throws Exception {
        def prepareTask = project.tasks.findByName("releasePrepare")
        prepareTask.execute()
    }

    @Test
    public void testAddPatchVersionName() throws Exception {
        def patchVersion = project.tasks.findByName("addPatchVersionName")
        patchVersion.execute()
    }

    @Test
    public void testTaskDepends() throws Exception {

//

        project.tasks.create('test1', TestTask1)
        project.tasks.create('test2', TestTask2)

        def t1 = project.tasks.findByName("test1")
        def t2 = project.tasks.findByName("test2")

//        project.tasks.test2.dependsOn test1
//        project.tasks.test2.execute()
//       project.tasks.test2.dependsOn  project.tasks.test1
//        project.tasks.test2.execute()

        def suffic = "test"
        project.tasks."${suffic}2".execute()
//        t2.dependsOn t1
//        t2.execute()
    }

   static class TestTask1 extends DefaultTask {

        @TaskAction
        def runTask() {
            println "test1"
        }
    }

    static class TestTask2 extends DefaultTask {

        @TaskAction
        def runTask() {
            println "test2"
        }
    }

    @Test
    public void testChannelTask() throws Exception {
        def channelReleaseTask = project.tasks.findByName("channelFromRelease")
        assertThat(channelReleaseTask, Matchers.notNullValue())

        Global global = Global.get(project)
        File apkFile = FileCreator.createApkFile(rootDir, "test_release.apk")
        global.apkFilePath = apkFile.absolutePath
        channelReleaseTask.execute()
    }

    @Test
    public void testPgyer() throws Exception {

        def apks = project.container(ApkTarget) {
            String apkName = WordUtils.capitalize(it.toString())
            def userTask = project.task("uploadPgyer${apkName}", type: PgyerUserUploadTask)
            userTask.group = 'Pgyer'
            userTask.description = "Upload an APK file of ${apkName}"
            userTask.apkName = apkName

            project.extensions.create(it, ApkTarget, apkName)
        }

        def pgyer = new PgyerExtension(apks)
        project.convention.plugins.deploygate = pgyer
        project.extensions.pgyer = pgyer

        def apkUpload = project.task('uploadPgyer', type: PgyerAllUploadTask)
        apkUpload.group = 'Pgyer'
        apkUpload.description = 'Uploads the APK file. Also updates the distribution specified by distributionKey if configured'


        project.pgyer {
            _api_key = "123"
            uKey = "1234"
        }

        project.apks {
            release {
                sourceFile = project.file("build.gradle")
            }
        }
    }
}
