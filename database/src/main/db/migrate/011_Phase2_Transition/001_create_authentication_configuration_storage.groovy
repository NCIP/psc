class PersistentConfiguration extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("authentication_system_conf") { t ->
            t.includePrimaryKey = false
            if (databaseMatches("sqlserver")) {
                t.addColumn("key_ms", "string", primaryKey: true, limit: 255)
            } else {
                t.addColumn("key", "string", primaryKey: true, limit: 255)
            }
            t.addColumn("value", "string")
            t.addVersionColumn()
        }
    }

    void down() {
        dropTable("authentication_system_conf", primaryKey: false)
    }
}
