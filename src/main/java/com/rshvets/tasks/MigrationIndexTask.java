package com.rshvets.tasks;

import org.gradle.api.logging.Logger;

import java.io.File;
import java.sql.Connection;

import static com.rshvets.utils.MigrationUtils.*;

public class MigrationIndexTask extends MigrationBaseTask {

    @Override
    protected void processDatabase(Logger logger, Connection connection,
                                   String host, Integer port, String dbName, String user, String password,
                                   String migrationSchema, String migrationTable) throws Exception {

        if (!checkMigrationSchema(logger, connection, migrationSchema, true))
            return;

        if (!checkMigrationTable(logger, connection, migrationSchema, migrationTable, true))
            return;

        cleanMigrationTable(connection, migrationSchema, migrationTable);

        for (File f : getScripts(getProject())) {
            String scriptName = f.getName();
            String scriptHash = getFileHash(f);

            insertMigrationRecord(connection, migrationSchema, migrationTable, scriptName, scriptHash);
        }
    }
}
