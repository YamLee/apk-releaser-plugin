package me.yamlee.apkrelease

import org.apache.commons.lang.WordUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.quanqi.pgyer.gradle.plugins.ApkTarget
import org.quanqi.pgyer.gradle.plugins.PgyerAllUploadTask
import org.quanqi.pgyer.gradle.plugins.PgyerExtension
import org.quanqi.pgyer.gradle.plugins.PgyerUserUploadTask


class ApkReleasePluginTest {
    Project project
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder()
                .withProjectDir(temporaryFolder.root)
                .build()
    }

    @Test
    public void testInitial() throws Exception {
        assert 1 == 1
    }

    @Test
    public void testApkReleasePlugin() throws Exception {
        project.apply plugin: 'com.android.application'
        project.apply plugin: 'me.yamlee.apkrelease'

        project.android {
            buildToolsVersion '23.0.2'

            //     <!-- 解决Android L上通知显示异常问题，targetSdkVersion需要设置成22 -->
            defaultConfig {
                applicationId "in.haojin.nearbymerchant"
                versionCode 100
                versionName "1.9.0"
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

        project.apkRelease.apkDistribute {
            haojin {
                pgyerApiKey = "fb914b7d5b72fc11622cafaa3dfb183f"
                pgyerUserKey = "ebdbfa7770bec238a0e9770e79459210"
            }
            release {
                pgyerApiKey = "3d109b16b9b16a8442eb601956c8f8af"
                pgyerUserKey = "9f7e464c5841eed38ef33709d5f8cd8a"
            }
        }


        def task = project.tasks.findByName("apkReleaseHaojin")
        task.execute()
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
