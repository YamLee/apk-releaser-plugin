package me.yamlee.apkrelease.internel

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat


class VcsAutoCommitorTest {
    VcsAutoCommitor vcsAutoCommitor
    String versionPropertyFilePath

    @Before
    public void setUp() throws Exception {
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
        vcsAutoCommitor.commitToVcs(project)
    }

    @Test
    public void testGetAndroidConfig() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.android.application'
        project.pluginManager.apply 'me.yamlee.apkrelease'
        def androidConfig = vcsAutoCommitor.getAndroidConfig(project)
        println androidConfig.versionName
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
}
