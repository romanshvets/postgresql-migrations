# postgresql-migrations
Gradle plugin for migrations on PostgreSQL database

## Rules of database migration
1. Store your scripts in VCS
2. Every script should be applied only once
3. Script should not change once applied

## What this plugin does?
*Short story*: it applies scripts one by one onto PostgreSQL database and builds a history of already applied scripts.

*Long story*. You should configure this plugin with 2 things:
- List of databases you need to migrate
- List of scripts you need to apply

The main task of this plugin - `applyMigration` - will do the following:
1. Checks that schema and table, where history of applied scripts is stored, exists and creates them if they're missing
2. Fetches the list of applies scripts from database
3. Compares this list with scripts you need to apply
4. Checks that already applied plugins did not change
5. Applies new scripts to database if nothing was found in previous step

## Plugin requirements
- `gradle 7.3.1` works fine for sure. Previous versions should work too. Did not check. Any help with detecting minimal acceptable Gradle version will be appreciated
- `PostreSQL 13.5` works well. Previous versions should work too. Did not check. Any help with detecting minimal acceptable PostgreSQL version will be appreciated

## How can I apply this plugin ?
Add this to your build script

```groovy
plugins {
    id "io.github.romanshvets.postgresql-migrations" version "1.0.4"
}
```

## How can I configure that plugin ?
You should add `migrations` closure with 2 properties


```groovy
migrations {
    scripts = fileTree(dir: "scripts").sort()   (1)

    databases {                                 (2)
        test {
            order = 1
            migrationSchema = "public"
            migrationTable = "migrations"
            connectionHost = "localhost"
            connectionPort = 5432
            dbName = "YOUR_DATABASE"
            user = "YOUR_USER"
            password = "YOUR_PASSWORD"
        }

        dev {
            order = 2
            migrationSchema = "public"
            migrationTable = "migrations"
            connectionHost = "localhost"
            connectionPort = 5432
            dbName = "YOUR_DATABASE"
            user = "YOUR_USER"
            password = "YOUR_PASSWORD"
        }
    }       
}
```

`(1)` - list of scripts to apply in `List<File>` format. Those scripts will be applied in the same order you provide them here  
`(2)` - list of database details. `test` and `dev` configurations are provided as example

## Ok, I configured. What's next? 
You can run any of three tasks:
- `checkMigration` - will check scripts and history table and print useful info without making any change to database
- `applyMigration` - will check scripts and history table and apply new scripts if everything is ok
- `indexMigration` - will rebuild history table from scratch without applying any script

## What a minute! What's history table?
It's a table where plugin will insert records for keep tracking of applied scripts. It will be automatically created by either `applyMigration` or `indexMigration`  task. Structure is following:
```roomsql
create table XXX.YYY
(
    id              serial primary key,
    script_name     varchar(256)                not null unique,
    script_hash     varchar(256)                not null,
    ts              timestamp without time zone not null
);
```
where
`XXX` is `migrationSchemaName` and `YYY` is `migrationSchemaTable`

Please contact me at <roman.shvets.1989@gmail.com> if you have any questions