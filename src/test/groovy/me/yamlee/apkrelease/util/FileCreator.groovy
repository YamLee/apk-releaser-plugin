package me.yamlee.apkrelease.util

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by yamlee on 7/9/16.
 */
class FileCreator {
    public static File createApkFile(File parentFile,String fileName) {
        File apkFile = new File(parentFile,fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(apkFile));
        ZipEntry e2 = new ZipEntry("META-INF/test.txt");
        out.putNextEntry(e2);
        StringBuilder sb = new StringBuilder();
        sb.append("Test String");
        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
        out.close();
        return apkFile
    }
}
