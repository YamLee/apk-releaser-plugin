package me.yamlee.apkrelease.internel.vcs

/**
 * Created by yamlee on 7/7/16.
 */
interface VcsOperator {
    /**
     * List all branch name
     * @return
     */
    List<String> branchList()

    /**
     * check out a branch with target branch name
     * @param branchName
     * @return
     */
    boolean checkOut(String branchName, boolean createNew)

    /**
     * Commit msg with param msg
     * @param msg
     * @return
     */
    boolean commit(String msg)

    /**
     * Get commit history log
     * @param recentCommitCount
     * @return
     */
    List<LogMessage> log(int recentCommitCount)

    /**
     * Get commit history log with range
     * @param recentCommitCount
     * @return
     */
    List<LogMessage> log(String fromCommitId,String toCommitId)

    /**
     * Get commit all history log
     * @param recentCommitCount
     * @return
     */
    List<LogMessage> logAll()

    /**
     * add a new tag
     * @param tagName
     * @param description
     * @return
     */
    boolean addTag(String tagName,String description)

    /**
     * push all tags to remote repository
     * @return
     */
    boolean pushTags()

    /**
     * push to remote repository
     * @return
     */
    boolean push()
}