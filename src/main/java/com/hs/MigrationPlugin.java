package com.hs;

import com.hs.tasks.MigrationApplyTask;
import com.hs.tasks.MigrationCheckTask;
import com.hs.tasks.MigrationIndexTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class MigrationPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create(MigrationPluginExtension.NAME, MigrationPluginExtension.class);

        TaskContainer tasks = project.getTasks();

        tasks.create("checkMigration", MigrationCheckTask.class);
        tasks.create("indexMigration", MigrationIndexTask.class);
        tasks.create("applyMigration", MigrationApplyTask.class);
    }
}
