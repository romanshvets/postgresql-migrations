package com.rshvets;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.ListProperty;

import java.io.File;

public interface MigrationPluginExtension {

    public static final String NAME = "migrations";

    NamedDomainObjectContainer<MigrationDatabaseDetails> getDatabases();

    ListProperty<File> getScripts();
}
