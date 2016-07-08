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
    public void testCommitToVcs() throws Exception {
        fakeVcsOperator.commit("*test1")
        fakeVcsOperator.commit("*测试")
        vcsAutoCommitor.commitMsgToVcs("*")
        assertThat(fakeVcsOperator.remoteTagList.get(0), is("v1.1.1_1234"))
        assertThat(fakeVcsOperator.remoteLogList.get(0).message, is("*test1"))
        assertThat(fakeVcsOperator.remoteLogList.get(1).message, is("*测试"))
    }


}
