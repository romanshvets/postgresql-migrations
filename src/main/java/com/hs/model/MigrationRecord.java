package com.hs.model;

import java.util.Date;

public class MigrationRecord {
    private final String scriptName;
    private final String scriptHash;
    private final Date ts;

    public MigrationRecord(String scriptName, String scriptHash, Date ts) {
        this.scriptName = scriptName;
        this.scriptHash = scriptHash;
        this.ts = ts;
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getScriptHash() {
        return scriptHash;
    }

    public Date getTs() {
        return ts;
    }
}
