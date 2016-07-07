package me.yamlee.apkrelease.internel.task

import me.yamlee.apkrelease.internel.VcsAutoCommitor
import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

/**
 * Created by yamlee on 6/21/16.
 */
class GitCommitTask extends DefaultTask {
    private static final Logger LOG = Logging.getLogger(GitCommitTask.class);


    @TaskAction
    def runTask() {
        LOG.info("------run git commit task------")
        VcsAutoCommitor commitor = new VcsAutoCommitor()
        commitor.run(project)
    }
}
