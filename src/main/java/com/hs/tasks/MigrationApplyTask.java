package com.hs.tasks;

import com.hs.MigrationPluginExtensionEntry;
import com.hs.model.MigrationDiff;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.hs.utils.MigrationUtils.*;
import static java.lang.String.format;

public class MigrationApplyTask extends DefaultTask {

    @TaskAction
    public void run() throws Exception {
        List<MigrationPluginExtensionEntry> dbConfigs = getDBConfigs(getProject());

        if (!checkDatabasesConfig(getLogger(), dbConfigs))
            return;

        for (final MigrationPluginExtensionEntry e : dbConfigs) {

            final String host = e.getConnectionHost();
            final Integer port = e.getConnectionPort();
            final String db = e.getDb();
            final String user = e.getUser();
            final String password = e.getPassword();

            try (Connection connection = acquireConnection(getLogger(), host, port, db, user, password)) {
                showProcessingMessage(getLogger(), e.getName(), db, host, port);

                final String schemaName = e.getMigrationSchema();
                final String tableName = e.getMigrationTable();

                if (!checkMigrationSchema(getLogger(), connection, schemaName, true))
                    return;

                if (!checkMigrationTable(getLogger(), connection, schemaName, tableName, true))
                    return;

                MigrationDiff diff = getMigrationsDiff(connection, schemaName, tableName, getScripts(getProject()));

                if (anyMigrationError(diff)) {
                    getLogger().error(migrationDiffErrors(diff));
                    throw new GradleException("Cannot migrate database. Please follow the errors above");
                }

                if (!anyNewScriptsToApply(diff)) {
                    getLogger().lifecycle(format("No new scripts found for %s on %s:%s", db, host, port));
                    continue;
                }

                List<File> scriptsToApply = diff.getScriptsWithoutRecords();

                getLogger().lifecycle(format("Applying %s %s to %s on %s:%s",
                        scriptsToApply.size(), scriptsToApply.size() == 1 ? "script" : "scripts", db, host, port));

                String connectionURL = format("postgresql://%s:%s@%s:%s/%s", e.getUser(), e.getPassword(),
                        e.getConnectionHost(), e.getConnectionPort(), e.getDb());

                for (File script : scriptsToApply) {
                    String fileParam = format("--file=%s", script.getPath());

                    ProcessBuilder builder = new ProcessBuilder("psql", fileParam, connectionURL);

                    Date start = new Date();
                    Process process = builder.start();

                    Optional<String> error = readStream(process.getErrorStream());
                    if (error.isPresent()) {
                        String message = format("Failed to apply script %s.\nError: %s", script.getName(), error.get());

                        getLogger().error(message);
                        return;
                    }

                    int resultCode = process.waitFor();
                    Date end = new Date();
                    if (resultCode != 0) {
                        String command = String.join(" ", builder.command());
                        String message = format("Command \"%s\" failed with exit code %s",
                                command, resultCode);

                        getLogger().error(message);
                        return;
                    }

                    long time = end.getTime() - start.getTime();
                    getLogger().lifecycle(format("* %s applied in %sms", script.getName(), time));

                    insertMigrationRecord(connection, schemaName, tableName, script.getName(), getFileHash(script));
                }
            }
        }
    }
}
