package com.rshvets.model;

import java.io.File;
import java.util.List;

public class MigrationDiff {

    private final List<MigrationRecord2Script> matchedRecordsWithScripts;
    private final List<File> scriptsWithoutRecords;
    private final List<MigrationRecord2Script> unmatchedRecordsWithScripts;
    private final List<MigrationRecord> recordsWithoutScripts;

    public MigrationDiff(
            List<MigrationRecord2Script> matchedRecordsWithScripts,
            List<File> scriptsWithoutRecords,
            List<MigrationRecord2Script> unmatchedRecordsWithScripts,
            List<MigrationRecord> recordsWithoutScripts) {

        this.matchedRecordsWithScripts = matchedRecordsWithScripts;
        this.scriptsWithoutRecords = scriptsWithoutRecords;
        this.unmatchedRecordsWithScripts = unmatchedRecordsWithScripts;
        this.recordsWithoutScripts = recordsWithoutScripts;
    }

    public List<MigrationRecord2Script> getMatchedRecordsWithScripts() {
        return matchedRecordsWithScripts;
    }

    public List<File> getScriptsWithoutRecords() {
        return scriptsWithoutRecords;
    }

    public List<MigrationRecord2Script> getUnmatchedRecordsWithScripts() {
        return unmatchedRecordsWithScripts;
    }

    public List<MigrationRecord> getRecordsWithoutScripts() {
        return recordsWithoutScripts;
    }

    public static class MigrationRecord2Script {
        private final MigrationRecord record;
        private final File file;

        public MigrationRecord2Script(MigrationRecord record, File file) {
            this.record = record;
            this.file = file;
        }

        public MigrationRecord getRecord() {
            return record;
        }

        public File getFile() {
            return file;
        }
    }
}
