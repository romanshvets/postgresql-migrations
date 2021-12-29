package com.rshvets.tasks;

import com.rshvets.MigrationDatabaseDetails;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.sql.Connection;
import java.util.List;

import static com.rshvets.utils.MigrationUtils.*;

public class MigrationBaseTask extends DefaultTask {

    @TaskAction
    public void run() throws Exception {
        List<MigrationDatabaseDetails> dbConfigs = getDBConfigs(getProject());

        if (!checkDatabasesConfig(getLogger(), dbConfigs))
            return;

        for (MigrationDatabaseDetails e : dbConfigs) {
            String host = e.connectionHost;
            Integer port = e.connectionPort;
            String db = e.dbName;
            String user = e.user;
            String password = e.password;

            try (Connection connection = acquireConnection(getLogger(), host, port, db, user, password)) {

            }
        }
    }

    protected void processDatabase(String host, Integer port, String db, String user, String password,
                                   String migrationSchemaName, String migrationTableName) throws Exception {

        showProcessingMessage(getLogger(), e.name, db, host, port);
    }
}
