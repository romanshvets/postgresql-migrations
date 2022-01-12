package com.rshvets.tasks;

import com.rshvets.model.MigrationDiff;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.rshvets.utils.MigrationUtils.*;
import static java.lang.String.format;

public class MigrationApplyTask extends MigrationBaseTask {

    @Override
    protected void processDatabase(Logger logger, Connection connection,
                                   String host, Integer port, String dbName, String user, String password,
                                   String migrationSchema, String migrationTable) throws Exception {

        if (!checkMigrationSchema(logger, connection, migrationSchema, true))
            return;

        if (!checkMigrationTable(logger, connection, migrationSchema, migrationTable, true))
            return;

        MigrationDiff diff = getMigrationsDiff(connection, migrationSchema, migrationTable, getScripts(getProject()));

        if (anyMigrationError(diff)) {
            logger.error(migrationDiffErrors(diff));
            throw new GradleException("Cannot migrate database. Please follow the errors above");
        }

        if (!anyNewScriptsToApply(diff)) {
            logger.lifecycle("No new scripts found");
            return;
        }

        List<File> scriptsToApply = diff.getScriptsWithoutRecords();

        String connectionURL = format("postgresql://%s:%s@%s:%s/%s", user, password, host, port, dbName);

        for (File script : scriptsToApply) {
            String fileParam = format("--file=%s", script.getPath());

            ProcessBuilder builder = new ProcessBuilder("psql", fileParam, connectionURL);

            Date start = new Date();
            Process process = builder.start();

            Optional<String> error = readStream(process.getErrorStream());
            if (error.isPresent()) {
                logger.error("Failed to apply script " + script.getName());
                logger.error(error.get());
                return;
            }

            int resultCode = process.waitFor();
            Date end = new Date();
            if (resultCode != 0) {
                String command = String.join(" ", builder.command());
                String message = format("Command \"%s\" failed with exit code %s",
                        command, resultCode);

                logger.error(message);
                return;
            }

            long time = end.getTime() - start.getTime();
            logger.lifecycle(format("* %s applied in %sms", script.getName(), time));

            insertMigrationRecord(connection, migrationSchema, migrationTable, script.getName(), getFileHash(script));
        }
    }
}
