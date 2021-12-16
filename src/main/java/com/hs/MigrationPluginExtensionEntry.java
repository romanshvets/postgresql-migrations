package com.hs;

public class MigrationPluginExtensionEntry {

    private final String name;
    private final Integer order;

    private final String migrationSchema;
    private final String migrationTable;

    private final String connectionHost;
    private final Integer connectionPort;
    private final String db;
    private final String user;
    private final String password;

    public MigrationPluginExtensionEntry(String name, Integer order, String migrationSchema,
                                         String migrationTable, String connectionHost,
                                         Integer connectionPort, String db, String user, String password) {
        this.name = name;
        this.order = order;
        this.migrationSchema = migrationSchema;
        this.migrationTable = migrationTable;
        this.connectionHost = connectionHost;
        this.connectionPort = connectionPort;
        this.db = db;
        this.user = user;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    public String getMigrationSchema() {
        return migrationSchema;
    }

    public String getMigrationTable() {
        return migrationTable;
    }

    public String getConnectionHost() {
        return connectionHost;
    }

    public Integer getConnectionPort() {
        return connectionPort;
    }

    public String getDb() {
        return db;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
