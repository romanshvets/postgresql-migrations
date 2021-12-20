package com.rshvets;

import com.rshvets.tasks.MigrationApplyTask;
import com.rshvets.tasks.MigrationCheckTask;
import com.rshvets.tasks.MigrationIndexTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

public class MigrationPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        extensions.create(MigrationPluginExtension.NAME, MigrationPluginExtension.class);

        TaskContainer tasks = project.getTasks();
        tasks.create("checkMigration", MigrationCheckTask.class);
        tasks.create("indexMigration", MigrationIndexTask.class);
        tasks.create("applyMigration", MigrationApplyTask.class);
    }
}
