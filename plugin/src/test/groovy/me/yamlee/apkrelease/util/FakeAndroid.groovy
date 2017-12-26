package me.yamlee.apkrelease.util

import me.yamlee.apkrelease.internel.Android

/**
 * Created by yamlee on 7/8/16.
 */
class FakeAndroid implements Android{
    @Override
    String getApkVersionName() {
        return "1.1.1_1234"
    }

    @Override
    int getVersionCode() {
        return 1234
    }

    @Override
    String getVersionName() {
        return "1.1.1"
    }
}
