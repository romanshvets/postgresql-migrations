package com.rshvets.tasks;

import com.rshvets.MigrationDatabaseDetails;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import static com.rshvets.utils.MigrationUtils.*;

public class MigrationIndexTask extends DefaultTask {

    @TaskAction
    public void run() throws Exception {
        List<MigrationDatabaseDetails> dbConfigs = getDBConfigs(getProject());

        if (!checkDatabasesConfig(getLogger(), dbConfigs))
            return;

        for (final MigrationDatabaseDetails e : dbConfigs) {
            final String host = e.connectionHost;
            final Integer port = e.connectionPort;
            final String db = e.dbName;
            final String user = e.user;
            final String password = e.password;

            try (Connection connection = acquireConnection(getLogger(), host, port, db, user, password)) {
                showProcessingMessage(getLogger(), e.name, db, host, port);

                final String schemaName = e.migrationSchema;
                final String tableName = e.migrationTable;

                if (!checkMigrationSchema(getLogger(), connection, schemaName, true))
                    return;

                if (!checkMigrationTable(getLogger(), connection, schemaName, tableName, true))
                    return;

                cleanMigrationTable(connection, schemaName, tableName);

                for (File f : getScripts(getProject())) {
                    String scriptName = f.getName();
                    String scriptHash = getFileHash(f);

                    insertMigrationRecord(connection, schemaName, tableName, scriptName, scriptHash);
                }
            }
        }
    }
}
