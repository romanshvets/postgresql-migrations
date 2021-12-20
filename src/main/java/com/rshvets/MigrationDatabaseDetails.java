package com.rshvets;

public class MigrationDatabaseDetails {

    public final String name;

    public Integer order;

    public String migrationSchema;
    public String migrationTable;

    public String connectionHost;
    public Integer connectionPort;
    public String dbName;
    public String user;
    public String password;

    public MigrationDatabaseDetails(final String name) {
        this.name = name;
    }
}
