class CreateChangesTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("changes") { t ->
            t.addVersionColumn()
            t.addColumn("action", "string", nullable: false, limit: 8)
            t.addColumn("new_value", "string", nullable: true)
            t.addColumn("old_value", "string", nullable: true)
            t.addColumn("attribute", "string", nullable: true)
            t.addColumn("delta_id", "integer", nullable: false)
            t.addColumn("grid_id", "string", nullable: true, limit: 255)
        }
    }

    void down() {
        dropTable("changes");
    }
}
