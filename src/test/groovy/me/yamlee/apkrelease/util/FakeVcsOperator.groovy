package me.yamlee.apkrelease.util

import me.yamlee.apkrelease.internel.vcs.LogMessage
import me.yamlee.apkrelease.internel.vcs.VcsOperator

/**
 * Created by yamlee on 7/8/16.
 */
class FakeVcsOperator implements VcsOperator {
    private List<String> branchList
    private List<LogMessage> logList
    private List<String> tagList
    List<String> remoteTagList
    List<LogMessage> remoteLogList

    FakeVcsOperator() {
        branchList = new ArrayList<>()
        logList = new ArrayList<>()
        tagList = new ArrayList<>()
        remoteLogList = new ArrayList<>()
        remoteTagList = new ArrayList<>()
    }

    @Override
    List<String> branchList() {
        return branchList
    }

    @Override
    boolean checkOut(String branchName, boolean createNew) {
        return true
    }

    @Override
    boolean commit(String msg) {
        LogMessage logMessage = new LogMessage()
        logMessage.message = msg
        logMessage.id = UUID.randomUUID().toString()
        logList.add(logMessage)
        return true
    }

    @Override
    List<LogMessage> log(int recentCommitCount) {
        def listSize = logList.size()
        def index = (listSize - recentCommitCount) < 0 ? 0 : (listSize - recentCommitCount)
        List<LogMessage> tmpList = new ArrayList<>()
        for (int i = index; i < listSize; i++) {
            tmpList.add(logList.get(i))
        }
        return tmpList
    }

    @Override
    List<LogMessage> log(String fromCommitId, String toCommitId) {
        return logList
    }

    @Override
    List<LogMessage> logAll() {
        return logList
    }

    @Override
    boolean addTag(String tagName, String description) {
        tagList.add(tagName)
        return true;
    }

    @Override
    boolean pushTags() {
        remoteTagList.addAll(tagList)
        return true
    }

    @Override
    boolean push() {
        remoteLogList.addAll(logList)
        return true
    }
}
