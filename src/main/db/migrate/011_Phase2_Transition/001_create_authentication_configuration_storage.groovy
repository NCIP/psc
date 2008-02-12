class PersistentConfiguration extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("authentication_system_configuration") { t ->
            t.includePrimaryKey = false
            t.addColumn("key", "string", primaryKey: true)
            t.addColumn("value", "string")
            t.addVersionColumn()
        }
    }

    void down() {
        dropTable("authentication_system_configuration", primaryKey: false)
    }
}
