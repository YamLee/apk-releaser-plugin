package me.yamlee.apkrelease.internel

import me.yamlee.apkrelease.Constants
import me.yamlee.apkrelease.util.FileCreator
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Created by yamlee on 7/6/16.
 */
class ChannelApkGeneratorTest {
    private Project project
    private ChannelApkGenerator generator

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()
    private File rootDir
    private File apkFile
    @Before
    void setUp() {
        rootDir = temporaryFolder.newFolder("project")
        project = ProjectBuilder.builder()
                .withProjectDir(rootDir)
                .build()

        apkFile = FileCreator.createApkFile(rootDir,"test.apk");
        generator = new ChannelApkGenerator(apkFile.absolutePath)
    }

    @Test
    void testExecute() {
        File file = new File(rootDir, Constants.CHANNEL_FILE_NAME)
        file.createNewFile()
        Properties properties = new Properties()
        properties.setProperty("haojin", "好近渠道")
        file.withOutputStream { outputStream ->
            properties.store(outputStream, "channel_name=description")
        }
        generator.execute(project)
        File generatedApkFile = new File(rootDir, "test_haojin.apk")
        String channelName = generator.readChannel(generatedApkFile)
        MatcherAssert.assertThat(channelName, Matchers.is("channel_haojin"))
    }

    @Test
    public void testAppendApkNameWithChannel() throws Exception {
        String result = generator.appendApkNameWithChannel("haojin", "test_v1.0.1_build1234.apk")
        MatcherAssert.assertThat(result, Matchers.is("test_v1.0.1_build1234_haojin.apk"))
    }

    @Test
    public void testGetChannelList() throws Exception {
        File file = new File(rootDir, Constants.CHANNEL_FILE_NAME)
        file.createNewFile()
        Properties properties = new Properties()
        properties.setProperty("haojin", "好近渠道")
        file.withOutputStream { outputStream ->
            properties.store(outputStream, "channel_name=description")
        }
        List<String> channleList = generator.getChannelList(project)
        MatcherAssert.assertThat(channleList.size(), Matchers.is(1))
        MatcherAssert.assertThat(channleList.get(0), Matchers.is("haojin"))
    }


}
