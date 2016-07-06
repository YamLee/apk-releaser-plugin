package me.yamlee.apkrelease

import com.sun.nio.zipfs.ZipFileSystem
import org.junit.Test

import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * Created by yamlee on 7/6/16.
 */
class ZipCompressorTest {
    @Test
    public void testUnzip() throws Exception {
        def projectDir = System.getProperty("user.dir")
        File file = new File(projectDir + File.separator + "test.apk")
        File outputDirs = new File(projectDir + File.separator + "output")
        outputDirs.mkdirs()
        ZipCompressor zipCompressor = new ZipCompressor()
        zipCompressor.unzip(file.absolutePath, outputDirs.absolutePath)
        String newChannelMeta = outputDirs.absolutePath + File.separator + "META-INF" + File.separator + "haojin"
        File newFile = new File(newChannelMeta)
        newFile.createNewFile()
    }

    @Test
    public void testZip() throws Exception {
        def projectDir = System.getProperty("user.dir")
        File outputFile = new File(projectDir + File.separator + "test2.apk")
        File srcDirs = new File(projectDir + File.separator + "output")
        ZipCompressor zipCompressor = new ZipCompressor()
        zipCompressor.zipCompress(srcDirs.absolutePath, outputFile.absolutePath)
    }

    @Test
    public void testAdd() throws Exception {
        def projectDir = System.getProperty("user.dir")
        File outputFile = new File(projectDir + File.separator + "test2.apk")
        File srcFile = new File(projectDir + File.separator + "test.apk")
        ZipCompressor zipCompressor = new ZipCompressor()
        zipCompressor.add(srcFile.absolutePath, outputFile.absolutePath)
    }

    @Test
    public void testAppend() throws Exception {
        def projectDir = System.getProperty("user.dir")
        File contentFile = new File(projectDir + File.separator + "channel_haojin")
        File srcFile = new File(projectDir + File.separator + "test.apk")
        ZipCompressor zipCompressor = new ZipCompressor()
        File[] files = new File[1];
        files[0] =  contentFile
        zipCompressor.addFilesToZip(srcFile, files, "/META-INF/")
    }


}
