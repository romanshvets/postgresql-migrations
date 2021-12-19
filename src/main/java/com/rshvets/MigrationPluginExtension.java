package com.rshvets;

import java.io.File;
import java.util.List;

public class MigrationPluginExtension {

    public static final String NAME = "migrations";

    public List<MigrationDatabaseDetails> databases;
    public File[] scripts;
}
