package me.yamlee.apkrelease.internel.iml

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
class AndroidProxyTest{
    AndroidProxy androidProxy
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()
    File rootDir
    Project project

    @Before
    public void setUp() throws Exception {
        rootDir = temporaryFolder.newFolder("project")
        project = ProjectBuilder.builder()
                .withProjectDir(rootDir)
                .build()
        androidProxy =new AndroidProxy(project)

    }

    @Test
    public void testGetAndroidConfig() throws Exception {
        project.apply plugin: 'com.android.application'
        project.apply plugin: 'me.yamlee.apkrelease'
        project.android {
            def global = project.extensions.getByName("ext")
            defaultConfig {
                defaultConfig {
                    applicationId "me.yamlee.demo"
                    minSdkVersion 14
                    targetSdkVersion 21
                    versionCode global.versionCode
                    versionName global.versionName
                }
            }
        }
        def version = androidProxy.apkVersionName
        assertThat(version, is("1.0.0_1"))
        assertThat(androidProxy.versionCode,is(1))
    }
}
