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

class VcsAutoCommitorTest {
    VcsAutoCommitor vcsAutoCommitor
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
        vcsAutoCommitor = new VcsAutoCommitor(project, fakeVcsOperator, fakeAndroid)
    }

    @Test
    public void testCreatePropertyFileIfNotExist() throws Exception {
        println versionPropertyFilePath
        vcsAutoCommitor.createVersionPropertiesFileIfNotExist(versionPropertyFilePath)

        def file = new File(versionPropertyFilePath)
        assertThat(file.exists(), is(true))
        Properties properties = new Properties()
        file.withInputStream { inputStream ->
            properties.load(inputStream)
        }
        assertThat(properties.getProperty(VcsAutoCommitor.VERSION_CODE_KEY), is("1"))
        assertThat(properties.getProperty(VcsAutoCommitor.VERSION_NAME_PATCH_KEY), is("0"))
    }

    @Test
    public void testVersionCodeAdd() throws Exception {
        vcsAutoCommitor.createVersionPropertiesFileIfNotExist(versionPropertyFilePath)
        vcsAutoCommitor.versionCodeAdd(versionPropertyFilePath)
        File file = new File(versionPropertyFilePath)
        Properties properties = new Properties()
        properties.load(new FileInputStream(file))
        String patchVersion = properties.getProperty(VcsAutoCommitor.VERSION_NAME_PATCH_KEY)
        assertThat(patchVersion, is("1"))
        String versionCode = properties.getProperty(vcsAutoCommitor.VERSION_CODE_KEY)
        assertThat(versionCode, is("2"))
        file.delete()
    }

    @Test
    public void testGenerateChangeLog() throws Exception {
        vcsAutoCommitor.createVersionPropertiesFileIfNotExist(Constants.releaseFilePath(project))
        fakeVcsOperator.commit("*test1")
        fakeVcsOperator.commit("*测试")
        fakeVcsOperator.commit("test2")
        vcsAutoCommitor.generateChangeLog("1.0.0", "*")
        File file= new File(Constants.changeLogFilePath(project))
        FileReader fileReader = new FileReader(file)
        List<String> list = fileReader.readLines()
        assertThat(list.get(0), is("Change Log for 1.0.0:"))
        assertThat(list.get(1), is("1. test1"))
        assertThat(list.get(2), is("2. 测试"))

    }


    @Test
    public void testCommitToVcs() throws Exception {
        vcsAutoCommitor.commitMsgToVcs("*")
        fakeVcsOperator.branchList()
    }




}
