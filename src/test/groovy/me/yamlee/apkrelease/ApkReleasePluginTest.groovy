package me.yamlee.apkrelease

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


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
        project.apkRelease.item {
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
}
