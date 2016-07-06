package me.yamlee.apkrelease

import org.gradle.api.GradleException

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Created by yamlee on 7/6/16.
 */
class ZipCompressor {


    public boolean zipCompress(String srcFilePath, String desFile) {
        boolean isSuccessful = false;
        List<String> srcFileList = getFileList(srcFilePath)

//        String[] fileNames = new String[srcFileList.size()];
//        for (int i = 0; i < srcFileList.size() - 1; i++) {
//            fileNames[i] = parse(srcFileList.get(i));
//        }

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(desFile));
            ZipOutputStream zos = new ZipOutputStream(bos);
            String entryName = null;

            for (int i = 0; i < srcFileList.size(); i++) {
                entryName = srcFileList.get(i).replace("/Users/yamlee/GitHubProjects/apk-release-plugin/output", "");
                println entryName
                // 创建Zip条目
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);

                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFileList.get(i)));

                byte[] b = new byte[1024];

                while (bis.read(b, 0, 1024) != -1) {
                    zos.write(b, 0, 1024);
                }
                bis.close();
                zos.closeEntry();
            }

            zos.flush();
            zos.close();
            isSuccessful = true;
        } catch (IOException e) {
            throw e
        }

        return isSuccessful;
    }

    // 解析文件名
    private String parse(String srcFile) {
        int location = srcFile.lastIndexOf("/");
        String fileName = srcFile.substring(location + 1);
        return fileName;
    }

    /*
    * @param srcZipFile 需解压的文件名
    * @return  如果解压成功返回true
    */

    public boolean unzip(String srcZipFile, String desFileDirPath) {
        boolean isSuccessful = true;
        try {
            File destFile = new File(desFileDirPath)
            if (destFile != null && !destFile.directory) {
                throw new GradleException("unzip package dest dir can not be a file")
            }
            if (destFile != null && !destFile.exists()) {
                destFile.mkdirs()
            }
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcZipFile));
            ZipInputStream zis = new ZipInputStream(bis);

            BufferedOutputStream bos = null;

            //byte[] b = new byte[1024];
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                File file = new File(destFile.absolutePath, entryName)
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
//                if(file.exists())
                bos = new BufferedOutputStream(new FileOutputStream(file));

                int b = 0;
                while ((b = zis.read()) != -1) {
                    bos.write(b);
                }
                bos.flush();
                bos.close();
            }
            zis.close();
        } catch (IOException e) {
            isSuccessful = false;
            throw e

        }
        return isSuccessful;
    }

    public List<String> getFileList(String strPath) {
        List<String> fileList = new ArrayList<>()
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    List<String> fileListTmp = getFileList(files[i].getAbsolutePath()); // 获取文件绝对路径
                    fileList.addAll(fileListTmp);
                } else {
                    String strFileName = files[i].getAbsolutePath();
                    println("add file : " + strFileName);
                    fileList.add(strFileName);
                }
            }

        }
        return fileList;
    }



    private static final byte[] BUFFER = new byte[1024 * 1024 *30];
    /**
     * copy input to output stream - available in several StreamUtils or Streams classes
     */
    public  void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER))!= -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }

    public void add(String srcFilePath,String destFilePath) throws Exception {
        // read war.zip and write to append.zip
        ZipFile war = new ZipFile(srcFilePath);
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(destFilePath));

        // first, copy contents from existing war
        Enumeration<? extends ZipEntry> entries = war.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            System.out.println("copy: " + e.getName());
            append.putNextEntry(e);
            if (!e.isDirectory()) {
                copy(war.getInputStream(e), append);
            }
            append.closeEntry();
        }

        // now append some extra content
        ZipEntry e = new ZipEntry("answer.txt");
        System.out.println("append: " + e.getName());
        append.putNextEntry(e);
//        append.write("42\n".getBytes());
        append.closeEntry();

        // close
        war.close();
        append.close();
    }



    public void addFilesToZip(File source, File[] files, String path){
        try{
            File tmpZip = File.createTempFile(source.getName(), null);
            tmpZip.delete();
            if(!source.renameTo(tmpZip)){
                throw new Exception("Could not make temp file (" + source.getName() + ")");
            }
            byte[] buffer = new byte[4096];
            ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(source));
            for(int i = 0; i < files.length; i++){
                InputStream inputStream = new FileInputStream(files[i]);
                out.putNextEntry(new ZipEntry(path + files[i].getName()));
                for(int read = inputStream.read(buffer); read > -1; read = inputStream.read(buffer)){
                    out.write(buffer, 0, read);
                }
                out.closeEntry();
                inputStream.close();
            }
            for(ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()){
                if(!zipEntryMatch(ze.getName(), files, path)){
                    out.putNextEntry(ze);
                    for(int read = zin.read(buffer); read > -1; read = zin.read(buffer)){
                        out.write(buffer, 0, read);
                    }
                    out.closeEntry();
                }
            }
            out.close();
            tmpZip.delete();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean zipEntryMatch(String zeName, File[] files, String path){
        for(int i = 0; i < files.length; i++){
            if((path + files[i].getName()).equals(zeName)){
                return true;
            }
        }
        return false;
    }

    public void appendFile() {
        /* Define ZIP File System Properies in HashMap */
        Map<String, String> zip_properties = new HashMap<>();
        /* We want to read an existing ZIP File, so we set this to False */
        zip_properties.put("create", "false");
        /* Specify the encoding as UTF -8 */
        zip_properties.put("encoding", "UTF-8");
        /* Specify the path to the ZIP File that you want to read as a File System */
        URI zip_disk = URI.create("jar:file:/my_zip_file.zip");
    }
}
