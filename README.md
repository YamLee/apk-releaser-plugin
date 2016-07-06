# apk-release-plugin

> Android Gradle插件，主要用于简化Android Apk打包流程

当前功能包括（Beta）

1. 打包自动修改apk文件名称；
2. 打包时自动升级版本号和build号；
3. 打包完成后自动提交git，并创建相应的tag
4. 打包完成后自动上传pgyer第三方分发平台
5. 多渠道打包，秒级


## 项目依赖

在项目根目录**build.grade**文件中增加如下代码


```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.me.yamlee:apk-release-plugin:0.1.3"
  }
}
```

在应用模块目录**build.grade**文件中增加如下代码

```
apply plugin: "me.yamlee.apkrelease"
```

## 多渠道打包获取渠道名称方法

由于多渠道打包采用一种特殊的方法所以获取渠道信息不同于以往方法

```java
public static String getChannel(Context context) {
    String channel = null;
    String sourceDir = context.getApplicationInfo().sourceDir;
    final String start_flag = "META-INF/channel_";
    ZipFile zipfile = null;
    try {
        zipfile = new ZipFile(sourceDir);
        Enumeration<?> entries = zipfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            String entryName = entry.getName();
            if (entryName.contains(start_flag)) {
                channel = entryName.replace(start_flag, "");
                break;
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (zipfile != null) {
            try {
                zipfile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return channel;
}
```