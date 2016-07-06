package me.yamlee.apkrelease.internel

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

/**
 * Created by yamlee on 7/6/16.
 */
class ChannelApkGeneratorTest {
    private Project project


    @Before
    void setUp() {
        String userDir = System.getProperty("user.dir")
        File file = new File(userDir)
        println userDir
        project = ProjectBuilder.builder()
                .withProjectDir(file)
                .build()
    }

    @Test
    void testExecute() {
        ChannelApkGenerator channelPackager = new ChannelApkGenerator()
        channelPackager.execute(project)
    }

    @Test
    public void testReadChannel() throws Exception {
        ChannelApkGenerator generator = new ChannelApkGenerator()
        String channel = generator.readChannel(new File(project.getRootDir().absolutePath +
                File.separator + "test-haojin.apk"))
        println "channel $channel"

    }
}
