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
  dependencies {
    classpath "me.yamlee.apkrelease:plugin:0.3.2"
  }
}
```

在应用模块目录**build.grade**文件中增加如下代码

```
apply plugin: "me.yamlee.apkrelease"
```

## build.gradle文件中的详细配置

```
apply plugin: 'com.android.application'
apply plugin: "me.yamlee.apkrelease"

android {
    def global = project.extensions.ext
    compileSdkVersion 24
    buildToolsVersion "24.0.0"

    defaultConfig {
        applicationId "me.yamlee.demo"
        minSdkVersion 15
        targetSdkVersion 24
        versionCode global.versionCode
        versionName global.versionName
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
        debug {
            signingConfig signingConfigs.debug
        }
    }

    productFlavors {
        //市场版本发布,release
        store {
        }
    }

}

apkRelease {
	//自动提取git提交历史是，提交记录使用的标示符
    logIdentifyTag = "*"
    //push到的分支
    branchName = 'test'
    
    apkDistribute {
        storeRelease {
        	//蒲公英使用的api key
            pgyerApiKey = "...."
            //蒲公英使用的用户key
            pgyerUserKey = "..."
            //设置是否自动创建tag并提交到版本控制系统,如:git,不设置,默认为true
            autoCommitToCVS = false
            //设置是否生成changelog,不设置,默认为true
            generateChangeLog = false;
            
        }
    }
}
```

### 通过上面的样例配置需要注意如下几点

1. apkDistribute中的子item必须与buildVaraint一致，如上面代码生成的buildVaraint是storeRelease和storeDebug,所以apkdistribute配置中的item必须为其中的一个或多个，因为apkDistribute任务会依赖assemble任务
2. pgyer第三方的key需要自己注册获取


### 一切配置成功后就会生成如下任务列表

* apkDistStoreRelease
* channelFromStoreRelease
* channelFromStoreReleaseWithNewBuild
* addPatchVersionName
* addMinorVersionName
* addMajorVersionName



在AndroidStudio的Gradle视窗中可以找到一个apkrelease的任务组，组下为相对应的任务

1. addPatchVersionName，addMinorVersionName，addMajorVersionName:为升级相应版本号的任务

2. apkDist**StoreRelease** : 其中加粗内容为buildVariant根据你在android中配置的buildType和buildFlavor自动改变，此任务会走一个完整的打包流程：提取changeLog -> 生成apk -> 在git中创建tag和提交打包信息 -> push到远程仓库

3. channelFrom**StoreRelease**: 多渠道打包，运行此任务，会自动在***./build/outputs/apk***目录下查找相应的apk，找到第一个便会执行多渠道打包任务，打包的渠道列表或通过项目根目录的**channel.properties**文件中获取，如果项目根目录中并未创建此文件，插件会自动回你创建，但是不会在其中添加任务渠道信息，所以渠道信息需要你自己添加：key=description,如：googlePlay=谷歌市场；多渠道使用的key便是googlePlay

4. channelFrom**StoreRelease**WithNewBuild:此任务与上一任务执行的步骤是相同的，唯一不同的是，它会重新执行一遍apk aseemble逻辑，即会重新生成apk，再进行多渠道打包

> 需要注意，升级版本号任务并不包含在apkDist任务中，如果要升级版本号，需要手动运行addPatchVersionName等系列任务或者手动修改**release.properties**文件中对应的版本信息。

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