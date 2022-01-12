package com.rshvets.tasks;

import com.rshvets.MigrationDatabaseDetails;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskAction;

import java.sql.Connection;
import java.util.List;

import static com.rshvets.utils.MigrationUtils.*;

public abstract class MigrationBaseTask extends DefaultTask {

    @TaskAction
    public void run() throws Exception {
        List<MigrationDatabaseDetails> dbConfigs = getDBConfigs(getProject());

        if (!checkDatabasesConfig(getLogger(), dbConfigs))
            return;

        for (MigrationDatabaseDetails e : dbConfigs) {
            String configName = e.name;
            String host = e.connectionHost;
            Integer port = e.connectionPort;
            String db = e.dbName;
            String user = e.user;
            String password = e.password;
            String migrationSchema = e.migrationSchema;
            String migrationTable = e.migrationTable;

            try (Connection connection = acquireConnection(getLogger(), host, port, db, user, password)) {
                showProcessingMessage(getLogger(), configName, db, host, port);

                processDatabase(getLogger(), connection, host, port, db, user, password, migrationSchema, migrationTable);
            }
        }
    }

    abstract protected void processDatabase(Logger logger, Connection connection,
                                            String host, Integer port, String dbName, String user, String password,
                                            String migrationSchema, String migrationTable) throws Exception;
}
