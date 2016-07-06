package me.yamlee.apkrelease.internel

import me.yamlee.apkrelease.ReleaseJob
import org.gradle.api.Project

/**
 * Created by yamlee on 7/1/16.
 * Used to package different channel apk
 */
class ChannelPackager implements ReleaseJob {

    @Override
    void execute(Project project) {
        String channelPath = project.getRootDir().getAbsolutePath() + File.separator + "channels.properties"
        File channelFile = new File(channelPath)
        if (!channelFile.exists()) {
           channelFile.createNewFile()
        }
        Properties properties = new Properties()
        FileInputStream fileInputStream = new FileInputStream(channelPath)
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream)
        properties.load(inputStreamReader)
        Set<Object> keys = properties.keySet();//返回属性key的集合
        List<String> channelList = new ArrayList<>()
        for(Object key:keys){
            println("key:"+key.toString()+",value:"+properties.get(key));
            channelList.add(key.toString())
        }

    }

}
