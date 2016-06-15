package me.yamlee.apkrelease

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test


class ApkReleasePluginTest {


    @Test
    public void testInitial() throws Exception {
        assert 1 == 1
    }

    @Test
    public void testApkReleasePlugin() throws Exception {
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'me.yamlee.apkrelease'

        def name = project.tasks.hello.name
        assert(name.equals("hello") )
    }
}
