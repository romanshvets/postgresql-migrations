package com.rshvets.tasks;

import com.rshvets.MigrationDatabaseDetails;
import com.rshvets.model.MigrationDiff;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.sql.Connection;
import java.util.List;

import static com.rshvets.utils.MigrationUtils.*;

public class MigrationCheckTask extends DefaultTask {

    @TaskAction
    public void run() throws Exception {
        List<MigrationDatabaseDetails> dbConfigs = getDBConfigs(getProject());

        if (!checkDatabasesConfig(getLogger(), dbConfigs))
            return;

        for (final MigrationDatabaseDetails e : dbConfigs) {
            final String host = e.getConnectionHost();
            final Integer port = e.getConnectionPort();
            final String db = e.getDb();
            final String user = e.getUser();
            final String password = e.getPassword();

            try (Connection connection = acquireConnection(getLogger(), host, port, db, user, password)) {
                showProcessingMessage(getLogger(), e.getName(), db, host, port);

                final String schemaName = e.getMigrationSchema();
                final String tableName = e.getMigrationTable();

                if (!checkMigrationSchema(getLogger(), connection, schemaName, false))
                    return;

                if (!checkMigrationTable(getLogger(), connection, schemaName, tableName, false))
                    return;

                MigrationDiff migrationDiff = getMigrationsDiff(connection, schemaName, tableName, getScripts(getProject()));

                String migrationDiffInfo = migrationDiffInfo(migrationDiff);
                if (!isEmpty(migrationDiffInfo))
                    getLogger().lifecycle(migrationDiffInfo);

                String migrationDiffErrors = migrationDiffErrors(migrationDiff);
                if (!isEmpty(migrationDiffErrors))
                    getLogger().error(migrationDiffErrors);
            }
        }
    }
}
