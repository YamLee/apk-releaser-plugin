package me.yamlee.apkrelease;

import org.gradle.api.Project;

/**
 * Created by yamlee on 7/1/16.
 */
public interface ReleaseJob {

    void execute(Project project);
}
