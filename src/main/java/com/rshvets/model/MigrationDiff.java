package com.rshvets.model;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MigrationDiff {

    private final Map<MigrationRecord, File> matchedRecordsWithScripts;
    private final List<File> scriptsWithoutRecords;
    private final Map<MigrationRecord, File> unmatchedRecordsWithScripts;
    private final List<MigrationRecord> recordsWithoutScripts;

    public MigrationDiff(
            Map<MigrationRecord, File> matchedRecordsWithScripts,
            List<File> scriptsWithoutRecords,
            Map<MigrationRecord, File> unmatchedRecordsWithScripts,
            List<MigrationRecord> recordsWithoutScripts) {

        this.matchedRecordsWithScripts = matchedRecordsWithScripts;
        this.scriptsWithoutRecords = scriptsWithoutRecords;
        this.unmatchedRecordsWithScripts = unmatchedRecordsWithScripts;
        this.recordsWithoutScripts = recordsWithoutScripts;
    }

    public List<MigrationRecord> getRecordsWithoutScripts() {
        return recordsWithoutScripts;
    }

    public List<File> getScriptsWithoutRecords() {
        return scriptsWithoutRecords;
    }

    public Map<MigrationRecord, File> getMatchedRecordsWithScripts() {
        return matchedRecordsWithScripts;
    }

    public Map<MigrationRecord, File> getUnmatchedRecordsWithScripts() {
        return unmatchedRecordsWithScripts;
    }
}
