package me.yamlee.apkrelease.internel.iml

import me.yamlee.apkrelease.internel.VcsOperator
import org.ajoberstar.grgit.Grgit

/**
 * Created by yamlee on 7/7/16.
 */
class GitVcsOperator implements VcsOperator {
    Grgit git

    GitVcsOperator() {
        git = Grgit.open()
    }

    @Override
    List<String> branchList() {
        def branches = git.branch.list()
        List<String> branchNames = new ArrayList<>()
        branches.each { String branchName ->
            println branchName
            branchNames.add(branchName)
        }
        return branches
    }

    @Override
    boolean checkOut(String branchName, boolean createNew) {
        git.checkout(branch: branchName, createBranch: createNew)
        return true
    }

    @Override
    boolean commit(String msg) {
        git.commit(message: msg, all: true)
        return true
    }

    @Override
    List<String> log(int recentCommitCount) {
        List<String> historyList = new ArrayList<>()
        def historys = git.log(maxCommits: recentCommitCount)
        historys.each{ history ->
            historyList.add(history.shortMessage)
        }
        return historyList
    }

    @Override
    boolean addTag(String tagName, String description) {
        git.tag.add(name: tagName, message: description)
        return true
    }

    @Override
    boolean pushTags() {
        git.push(tags: true)
        return true
    }

    @Override
    boolean push() {
        git.push(all: true)
        return true
    }
}
