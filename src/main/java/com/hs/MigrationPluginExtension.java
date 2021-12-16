package com.hs;

import java.io.File;
import java.util.List;

public class MigrationPluginExtension {

    public static final String NAME = "migrations";

    public List<MigrationPluginExtensionEntry> databases;
    public File[] scripts;
}
