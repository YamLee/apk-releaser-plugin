package me.yamlee.apkrelease.internel

import org.apache.commons.lang.WordUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.quanqi.pgyer.gradle.plugins.ApkTarget
import org.quanqi.pgyer.gradle.plugins.PgyerAllUploadTask
import org.quanqi.pgyer.gradle.plugins.PgyerExtension
import org.quanqi.pgyer.gradle.plugins.PgyerUserUploadTask

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat


class VcsAutoCommitorTest {
    VcsAutoCommitor vcsAutoCommitor
    String versionPropertyFilePath
    Project project

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()


    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder()
                .withProjectDir(temporaryFolder.root)
                .build()
        versionPropertyFilePath = System.getProperty("user.dir") + File.separator + "version.properties"
        vcsAutoCommitor = new VcsAutoCommitor()
    }

    @Test
    public void testCreatePropertyFileIfNotExist() throws Exception {
        println versionPropertyFilePath
        vcsAutoCommitor.createVersionPropertiesFileIfNotExist(versionPropertyFilePath)

        def file = new File(versionPropertyFilePath)
        assertThat(file.exists(), is(true))
        file.delete()
    }

    @Test
    public void testVersionCodeAdd() throws Exception {
        vcsAutoCommitor.createVersionPropertiesFileIfNotExist(versionPropertyFilePath)
        vcsAutoCommitor.versionCodeAdd(versionPropertyFilePath)
        File file = new File(versionPropertyFilePath)
        Properties properties = new Properties()
        properties.load(new FileInputStream(file))
        String patchVersion = properties.getProperty(VcsAutoCommitor.VERSION_NAME_PATCH_KEY)
        assertThat(patchVersion, is("2"))
        String versionCode = properties.getProperty(vcsAutoCommitor.VERSION_CODE_KEY)
        assertThat(versionCode, is("2"))
        file.delete()
    }

    @Test
    public void testCommitToVcs() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.android.application'
        project.pluginManager.apply 'me.yamlee.apkrelease'
        vcsAutoCommitor.commitMsgToVcs(project)
    }

    @Test
    public void testGetAndroidConfig() throws Exception {
        project.apply plugin: 'com.android.application'
        project.android {
            defaultConfig {
                defaultConfig {
                    applicationId "me.yamlee.demo"
                    minSdkVersion 14
                    targetSdkVersion 21
                    versionCode 1
                    versionName "1.0"
                }
            }
        }
        def version = vcsAutoCommitor.getApkVersion(project)
        assertThat(version, is("1.0_1"))
    }

    @Test
    public void testGenerateChangeLog() throws Exception {
        File file = new File(versionPropertyFilePath)
        if (!file.exists()) {
            file.createNewFile()
        }
        vcsAutoCommitor.generateChangeLog(versionPropertyFilePath)
        file.delete()
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

        project.pgyer.apks {
            release {
                sourceFile = file("[apk1 file path]")
            }
        }
    }
}
