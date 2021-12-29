package com.rshvets.utils;

import com.rshvets.MigrationDatabaseDetails;
import com.rshvets.MigrationPluginExtension;
import com.rshvets.model.MigrationDiff;
import com.rshvets.model.MigrationRecord;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class MigrationUtils {

    private static final Comparator<MigrationDatabaseDetails> DATABASES_CONFIG_COMPARATOR =
            Comparator.comparingLong(e -> e.order);

    public static boolean isEmpty(String v) {
        return v == null || v.length() == 0 || v.trim().length() == 0;
    }

    public static <T> boolean isEmpty(Collection<T> v) {
        return v == null || v.size() == 0;
    }

    public static MigrationPluginExtension getExtension(Project project) {
        return (MigrationPluginExtension)
                project.getExtensions().getByName(MigrationPluginExtension.NAME);
    }

    public static List<MigrationDatabaseDetails> getDBConfigs(Project project) {
        MigrationPluginExtension extension = getExtension(project);

        return extension.getDatabases()
                .stream()
                .sorted(DATABASES_CONFIG_COMPARATOR)
                .collect(Collectors.toList());
    }

    public static List<File> getScripts(Project project) {
        MigrationPluginExtension extension = getExtension(project);
        return extension.getScripts().get();
    }

    public static boolean checkDatabasesConfig(Logger logger, List<MigrationDatabaseDetails> configs) {
        final boolean isEmpty = isEmpty(configs);

        if (isEmpty)
            logger.warn("Databases configs are not provided. Nothing to do");

        return !isEmpty;
    }

    public static Connection acquireConnection(Logger logger, String host, Integer port, String dbName, String user, String password) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            logger.error("Cannot find org.postgresql.Driver on classpath");
        }

        String url = format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s",
                host, port, dbName, user, password);

        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            logger.error("Cannot acquire connection to " + url, e);
        }

        return conn;
    }

    public static boolean checkMigrationSchema(Logger logger, Connection connection, String schemaName, boolean createIfNotExists) {
        boolean schemaExists = false;

        try {
            String query = format("select schema_name from information_schema.schemata where schema_name = '%s'", schemaName);

            PreparedStatement statement = connection.prepareStatement(query);

            ResultSet resultSet = statement.executeQuery();
            schemaExists = resultSet.next();

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            throw new GradleException(e.getMessage(), e);
        }

        if (!schemaExists) {
            if (createIfNotExists) {

                logger.warn(format("Schema '%s' does not exist. Will create it", schemaName));

                try {
                    String query = format("create schema %s", schemaName);

                    PreparedStatement statement = connection.prepareStatement(query);

                    statement.executeUpdate();
                    statement.close();
                } catch (Exception exception) {
                    throw new GradleException(exception.getMessage(), exception);
                }

                schemaExists = checkMigrationSchema(logger, connection, schemaName, false);
                if (schemaExists)
                    logger.warn("Schema '%s' created");
            } else {
                logger.warn(format("Schema '%s' does not exist. Nothing to check", schemaName));
            }
        }

        return schemaExists;
    }

    public static boolean checkMigrationTable(Logger logger, Connection connection, String schemaName,
                                              String tableName, boolean createIfNotExists) {
        boolean tableExists = false;

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "select * from information_schema.tables where table_schema = ? and table_name = ?");

            statement.setString(1, schemaName);
            statement.setString(2, tableName);

            ResultSet resultSet = statement.executeQuery();

            tableExists = resultSet.next();

            statement.close();
            resultSet.close();

        } catch (Exception exception) {
            throw new GradleException(exception.getMessage(), exception);
        }

        if (!tableExists) {
            if (createIfNotExists) {
                logger.warn(format("Table '%s' is not available within '%s' schema. Will create it", tableName, schemaName));

                try {
                    String query = format("create table %s.%s (" +
                            "id serial primary key," +
                            "script_name varchar(256) not null unique," +
                            "script_hash varchar(256) not null," +
                            "ts timestamp without time zone not null)", schemaName, tableName);

                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.executeUpdate();
                    statement.close();
                } catch (Exception exception) {
                    throw new GradleException(exception.getMessage(), exception);
                }

                tableExists = checkMigrationTable(logger, connection, schemaName, tableName, false);
                if (tableExists)
                    logger.warn(format("Table '%s' created within '%s' schema", tableName, schemaName));
            } else {
                logger.warn(format("Table '%s' does not exist within '%s' schema. Nothing to check", tableName, schemaName));
            }
        }

        return tableExists;
    }

    public static void cleanMigrationTable(Connection connection, String schemaName, String tableName) {
        try {
            String query = format("delete from %s.%s", schemaName, tableName);

            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            statement.close();
        } catch (Exception exception) {
            throw new GradleException(exception.getMessage(), exception);
        }
    }

    public static void insertMigrationRecord(Connection connection, String schemaName, String tableName,
                                             String scriptName, String scriptHash) {

        try {
            String template = "insert into %s.%s (script_name, script_hash, ts) values ('%s', '%s', now())";
            String query = format(template, schemaName, tableName, scriptName, scriptHash);

            PreparedStatement statement = connection.prepareStatement(query);
            statement.executeUpdate();
            statement.close();
        } catch (Exception exception) {
            throw new GradleException(exception.getMessage(), exception);
        }
    }

    public static List<MigrationRecord> getMigrationRecords(Connection connection, String schemaName, String tableName) {
        List<MigrationRecord> records = new LinkedList<>();

        try {
            String query = format("select * from %s.%s", schemaName, tableName);

            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String scriptName = rs.getString("script_name");
                String scriptHash = rs.getString("script_hash");
                Date ts = rs.getTimestamp("ts");

                records.add(new MigrationRecord(scriptName, scriptHash, ts));
            }

            rs.close();
            statement.close();
        } catch (Exception exception) {
            throw new GradleException(exception.getMessage(), exception);
        }

        return records;
    }

    public static String getFileHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            md.update(byteArray, 0, bytesCount);
        }

        fis.close();

        byte[] bytes = md.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public static MigrationDiff getMigrationsDiff(Connection connection, String schemaName,
                                                  String tableName, List<File> scripts) throws Exception {

        List<MigrationRecord> allMigrationRecords = getMigrationRecords(connection, schemaName, tableName);

        Map<File, String> filesHashes = new HashMap<>();
        for (File script : scripts) {
            filesHashes.put(script, getFileHash(script));
        }

        List<File> scriptsWithoutRecords = scripts.stream()
                .filter(f -> {
                    String fileName = f.getName();

                    return allMigrationRecords.stream().noneMatch(r -> Objects.equals(r.getScriptName(), fileName));
                }).collect(Collectors.toList());

        List<MigrationRecord> recordsWithoutScripts = allMigrationRecords.stream().filter(e -> {
            String migrationScriptName = e.getScriptName();

            return filesHashes.keySet().stream()
                    .map(File::getName)
                    .noneMatch(fn -> Objects.equals(migrationScriptName, fn));
        }).collect(Collectors.toList());

        List<MigrationDiff.MigrationRecord2Script> matchedRecordsWithScripts = new LinkedList<>();
        List<MigrationDiff.MigrationRecord2Script> unmatchedRecordsWithScripts = new LinkedList<>();

        scripts.forEach(script -> {
            String scriptName = script.getName();
            String scriptHash = filesHashes.get(script);

            MigrationRecord record = allMigrationRecords.stream()
                    .filter(r -> Objects.equals(r.getScriptName(), scriptName))
                    .findFirst().orElse(null);

            if (record == null)
                return;

            MigrationDiff.MigrationRecord2Script record2Script =
                    new MigrationDiff.MigrationRecord2Script(record, script);

            if (Objects.equals(record.getScriptHash(), scriptHash)) {
                matchedRecordsWithScripts.add(record2Script);
            } else {
                unmatchedRecordsWithScripts.add(record2Script);
            }
        });

        return new MigrationDiff(matchedRecordsWithScripts, scriptsWithoutRecords,
                unmatchedRecordsWithScripts, recordsWithoutScripts);
    }

    public static boolean anyMigrationError(MigrationDiff diff) {
        List<MigrationRecord> recordsWithoutScripts =
                diff.getRecordsWithoutScripts();

        List<MigrationDiff.MigrationRecord2Script> unmatchedRecordsWithScripts =
                diff.getUnmatchedRecordsWithScripts();

        return !isEmpty(recordsWithoutScripts) || !isEmpty(unmatchedRecordsWithScripts);
    }

    public static boolean anyNewScriptsToApply(MigrationDiff diff) {
        final List<File> newScripts = diff.getScriptsWithoutRecords();
        return newScripts != null && !newScripts.isEmpty();
    }

    public static void showProcessingMessage(Logger logger, String name, String db, String host, Integer port) {
        logger.lifecycle(String.format("\nProcessing '%s' database [%s on %s:%s]", name, db, host, port));
    }

    public static String migrationDiffInfo(MigrationDiff diff) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n").append("Already applied following scripts:");

        List<MigrationDiff.MigrationRecord2Script> matchedRecordsWithScripts =
                diff.getMatchedRecordsWithScripts();

        if (matchedRecordsWithScripts.isEmpty()) {
            sb.append(" none");
        } else {
            sb.append("\n");

            matchedRecordsWithScripts.stream()
                    .map(MigrationDiff.MigrationRecord2Script::getRecord)
                    .forEach(r -> sb.append("* ").append(r.getScriptName()).append("\n"));
        }

        sb.append("\n").append("Scripts to be applied:");
        List<File> scriptsWithoutRecords = diff.getScriptsWithoutRecords();

        if (scriptsWithoutRecords.isEmpty()) {
            sb.append(" none");
        } else {
            sb.append("\n");

            for (File file : scriptsWithoutRecords) {
                sb.append("* ").append(file.getName()).append("\n");
            }
        }

        return sb.toString();
    }

    public static String migrationDiffErrors(MigrationDiff diff) {
        StringBuilder sb = new StringBuilder();

        List<MigrationDiff.MigrationRecord2Script> unmatchedRecordsWithScripts =
                diff.getUnmatchedRecordsWithScripts();

        if (!unmatchedRecordsWithScripts.isEmpty()) {
            sb.append("\n").append("Hash mismatches in following scripts:").append("\n");

            unmatchedRecordsWithScripts.forEach(r -> {
                String scriptName = r.getRecord().getScriptName();
                String newFileHash = null;

                try {
                    newFileHash = getFileHash(r.getFile());
                } catch (Exception e) {
                    String message = format("Cannot get hash of file %s", r.getFile().getName());
                    throw new GradleException(message, e);
                }

                String oldFileHash = r.getRecord().getScriptHash();

                sb.append(format("* %s. Was: %s but now is %s", scriptName, oldFileHash, newFileHash)).append("\n");
            });
        }

        List<MigrationRecord> recordsWithoutScripts = diff.getRecordsWithoutScripts();

        if (!recordsWithoutScripts.isEmpty()) {
            sb.append("\n").append("Following scripts are missing:").append("\n");

            for (MigrationRecord record : recordsWithoutScripts) {
                sb.append("* ").append(record.getScriptName()).append("\n");
            }
        }

        return sb.toString();
    }

    public static Optional<String> readStream(InputStream is) throws Exception {
        Optional<String> res = Optional.empty();

        String result;

        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            result = builder.toString();
        }

        if (result.trim().length() != 0)
            return Optional.of(result);

        return res;
    }
}
