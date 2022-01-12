package com.rshvets.tasks;

import com.rshvets.model.MigrationDiff;
import org.gradle.api.logging.Logger;

import java.sql.Connection;

import static com.rshvets.utils.MigrationUtils.*;

public class MigrationCheckTask extends MigrationBaseTask {

    @Override
    protected void processDatabase(Logger logger, Connection connection,
                                   String host, Integer port, String dbName, String user, String password,
                                   String migrationSchema, String migrationTable) throws Exception {

        if (!checkMigrationSchema(logger, connection, migrationSchema, false))
            return;

        if (!checkMigrationTable(logger, connection, migrationSchema, migrationTable, false))
            return;

        MigrationDiff migrationDiff = getMigrationsDiff(connection, migrationSchema, migrationTable, getScripts(getProject()));

        String migrationDiffInfo = migrationDiffInfo(migrationDiff);

        if (!isEmpty(migrationDiffInfo))
            logger.lifecycle(migrationDiffInfo);

        String migrationDiffErrors = migrationDiffErrors(migrationDiff);
        if (!isEmpty(migrationDiffErrors))
            logger.error(migrationDiffErrors);
    }
}
