package me.yamlee.apkrelease.internel

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.util.FakeAndroid
import me.yamlee.apkrelease.util.FakeVcsOperator
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

/**
 * Created by yamlee on 7/8/16.
 */
class ReleasePreparerTest {
    ReleasePreparer releasePreparer
    String versionPropertyFilePath
    Project project
    File rootDir
    FakeAndroid fakeAndroid
    FakeVcsOperator fakeVcsOperator
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()


    @Before
    public void setUp() throws Exception {
        rootDir = temporaryFolder.newFolder("project")
        project = ProjectBuilder.builder()
                .withProjectDir(rootDir)
                .build()
        versionPropertyFilePath = Constants.releaseFilePath(project)
        fakeAndroid = new FakeAndroid()
        fakeVcsOperator = new FakeVcsOperator()
        releasePreparer = new ReleasePreparer(project, fakeVcsOperator, fakeAndroid)
    }

    @Test
    public void testCreatePropertyFileIfNotExist() throws Exception {
        println versionPropertyFilePath
        releasePreparer.createVersionPropertiesFileIfNotExist(versionPropertyFilePath)

        def file = new File(versionPropertyFilePath)
        assertThat(file.exists(), is(true))
        Properties properties = new Properties()
        file.withInputStream { inputStream ->
            properties.load(inputStream)
        }
        assertThat(properties.getProperty(ReleasePreparer.KEY_VERSION_CODE), is("1"))
        assertThat(properties.getProperty(ReleasePreparer.KEY_VERSION_NAME), is("1.0.0"))
    }

    @Test
    public void testVersionCodeAdd() throws Exception {
        releasePreparer.createVersionPropertiesFileIfNotExist(versionPropertyFilePath)
        releasePreparer.setAddedVersionCode(versionPropertyFilePath, ReleasePreparer.VersionNameType.PATCH)
        File file = new File(versionPropertyFilePath)
        Properties properties = new Properties()
        properties.load(new FileInputStream(file))
        String patchVersion = properties.getProperty(ReleasePreparer.KEY_VERSION_NAME)
        assertThat(patchVersion, is("1.0.1"))
        String versionCode = properties.getProperty(releasePreparer.KEY_VERSION_CODE)
        assertThat(versionCode, is("2"))

        int code = project.extensions.ext.versionCode
        assertThat(code, is(2))
        String name = project.extensions.ext.versionName
        assertThat(name, is("1.0.1"))
        file.delete()
    }

    @Test
    public void testGenerateChangeLog() throws Exception {
        releasePreparer.createVersionPropertiesFileIfNotExist(Constants.releaseFilePath(project))
        fakeVcsOperator.commit("*test1")
        fakeVcsOperator.commit("*测试")
        fakeVcsOperator.commit("test2")
        releasePreparer.generateChangeLog("1.0.0", "*")
        File file = new File(Constants.changeLogFilePath(project))
        FileReader fileReader = new FileReader(file)
        List<String> list = fileReader.readLines()
        assertThat(list.get(0), is("Change Log for 1.0.0:"))
        assertThat(list.get(1), is("1. test1"))
        assertThat(list.get(2), is("2. 测试"))

    }

    @Test
    public void testPrepareApkVersionInfo() throws Exception {
        releasePreparer.prepareApkVersionInfo()
        String name = project.extensions.ext.versionName
        assertThat(name, is("1.0.0"))
        int code = project.extensions.ext.versionCode
        assertThat(code, is(1))
    }
}
