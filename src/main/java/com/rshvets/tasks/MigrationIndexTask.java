package com.rshvets.tasks;

import com.rshvets.MigrationPluginExtensionEntry;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import static com.rshvets.utils.MigrationUtils.*;

public class MigrationIndexTask extends DefaultTask {

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
