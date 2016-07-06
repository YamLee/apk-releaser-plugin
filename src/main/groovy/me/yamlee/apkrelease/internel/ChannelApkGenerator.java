package me.yamlee.apkrelease.internel;

import me.yamlee.apkrelease.ReleaseJob;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.io.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ChannelApkGenerator implements ReleaseJob {
    private static final String CHANNEL_PREFIX = "/META-INF/";
    private static final String CHANNEL_FILE_NAME = "channels.properties";
    private static final String FILE_NAME_CONNECTOR = "-";
    private static final String CHANNEL_FLAG = "channel_";
    private String apkFilePath;

    public ChannelApkGenerator(String apkFilePath) {
        this.apkFilePath = apkFilePath;
    }

    @Override
    public void execute(Project project) {
        String channelPath = project.getRootDir().getAbsolutePath() + File.separator + CHANNEL_FILE_NAME;
        File channelFile = new File(channelPath);
        if (!channelFile.exists()) {
            try {
                channelFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Properties properties = new Properties();
        FileInputStream fileInputStream = null ;
        try {
            fileInputStream = new FileInputStream(channelPath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            properties.load(inputStreamReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<Object> keys = properties.keySet();//返回属性key的集合
        List<String> channelList = new ArrayList<>();
        for (Object key : keys) {
            System.out.println("key:" + key.toString() + ",value:" + properties.get(key));
            channelList.add(key.toString());
        }

        File apkFile = new File(apkFilePath);
        if (!apkFile.exists()) {
            System.out.println("找不到文件：" + apkFile.getPath());
            return;
        }

        String existChannel;
        try {
            existChannel = readChannel(apkFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (existChannel != null) {
            System.out.println("此安装包已存在渠道号：" + existChannel + "，请使用原始包");
            return;
        }

        String parentDirPath = apkFile.getParent();
        if (parentDirPath == null) {
            System.out.println("请输入完整的文件路径：" + apkFile.getPath());
            return;
        }
        String fileName = apkFile.getName();

        int lastPintIndex = fileName.lastIndexOf(".");
        String fileNamePrefix;
        String fileNameSurfix;
        if (lastPintIndex != -1) {
            fileNamePrefix = fileName.substring(0, lastPintIndex);
            fileNameSurfix = fileName.substring(lastPintIndex, fileName.length());
        } else {
            fileNamePrefix = fileName;
            fileNameSurfix = "";
        }

//        LinkedList<String> channelList = getChannelList(new File(apkFile.getParentFile(), CHANNEL_FILE_NAME));
//        if (channelList == null) {
//            return;
//        }

        for (String channel : channelList) {
            String newApkPath = parentDirPath + File.separator + fileNamePrefix + FILE_NAME_CONNECTOR + channel + fileNameSurfix;
            try {
                copyFile(apkFilePath, newApkPath);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (!changeChannel(newApkPath, CHANNEL_FLAG + channel)) {
                break;
            }
        }
    }

    /**
     * 复制文件
     */
    private void copyFile(final String sourceFilePath, final String targetFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);
        if (targetFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            targetFile.delete();
        }

        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 8];
            int realReadLength;
            while ((realReadLength = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, realReadLength);
            }
        } catch (Exception e) {
            System.out.println("复制文件失败：" + targetFilePath);
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    /**
     * 添加渠道号，原理是在apk的META-INF下新建一个文件名为渠道号的文件
     */
    private boolean changeChannel(final String newApkFilePath, final String channel) {
        try (FileSystem fileSystem = createZipFileSystem(newApkFilePath, false)){
            final Path root = fileSystem.getPath(CHANNEL_PREFIX);
            ChannelFileVisitor visitor = new ChannelFileVisitor();
            try {
                Files.walkFileTree(root, visitor);
            } catch (IOException e) {
                e.printStackTrace();
                throw new GradleException("添加渠道号失败: $channel");
            }

            Path existChannel = visitor.getChannelFile();
            if (existChannel != null) {
                System.out.println("此安装包已存在渠道号：" + existChannel.getFileName().toString() + ", FilePath: " + newApkFilePath);
                return false;
            }

            Path newChannel = fileSystem.getPath(CHANNEL_PREFIX + channel);
            try {
                Files.createFile(newChannel);
            } catch (IOException e) {
                System.out.println("添加渠道号失败：" + channel);
                e.printStackTrace();
                return false;
            }

            System.out.println("添加渠道号成功：" + channel + ", NewFilePath：" + newApkFilePath);
            return true;
        } catch (IOException e) {
            System.out.println("添加渠道号失败：" + channel);
            e.printStackTrace();
            return false;
        }
    }

    private FileSystem createZipFileSystem(String zipFilename, boolean create) throws IOException {
        final Path path = Paths.get(zipFilename);
        final URI uri = URI.create("jar:file:" + path.toUri().getPath());

        final Map<String, String> env = new HashMap<>();
        if (create) {
            env.put("create", "true");
        }
        return FileSystems.newFileSystem(uri, env);
    }


    private static class ChannelFileVisitor extends SimpleFileVisitor<Path> {
        private Path channelFile;

        public Path getChannelFile() {
            return channelFile;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.getFileName().toString().startsWith(CHANNEL_FLAG)) {
                channelFile = file;
                return FileVisitResult.TERMINATE;
            } else {
                return FileVisitResult.CONTINUE;
            }
        }
    }

    String readChannel(File apkFile) throws IOException {
        FileSystem zipFileSystem;
        try {
            zipFileSystem = createZipFileSystem(apkFile.getPath(), false);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("读取渠道号失败：" + apkFile.getPath());
            throw e;
        }

        final Path root = zipFileSystem.getPath(CHANNEL_PREFIX);
        ChannelFileVisitor visitor = new ChannelFileVisitor();
        try {
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("读取渠道号失败：" + apkFile.getPath());
            throw e;
        }

        Path existChannel = visitor.getChannelFile();
        return existChannel != null ? existChannel.getFileName().toString() : null;
    }
}