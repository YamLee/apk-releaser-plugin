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
        project.apply plugin: 'me.yamlee.apkrelease'
        project.apkDistribute {
            preview {
                pgyerApiKey = "fb914b7d5b72fc11622cafaa3dfb183f"
                pgyerUserKey = "ebdbfa7770bec238a0e9770e79459210"
            }
            test {
                pgyerApiKey = "3d109b16b9b16a8442eb601956c8f8af"
                pgyerUserKey = "9f7e464c5841eed38ef33709d5f8cd8a"
            }
        }


        def task = project.tasks.findByName("apkReleasePreview")
        task.getActions().get(0).execute(task)
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
