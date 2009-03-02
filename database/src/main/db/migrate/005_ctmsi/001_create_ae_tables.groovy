class CreateAeTables extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("adverse_events") { t ->
            t.addVersionColumn()
            t.addColumn("detection_date", "date")
            t.addColumn("description", "string", nullable: false)
            t.addColumn("big_id", "string", limit: 255)
        }

        createTable("ae_notifications") { t ->
            t.addVersionColumn()
            t.addColumn("assignment_id", "integer", nullable: false)
            t.addColumn("adverse_event_id", "integer", nullable: false)
            t.addColumn("dismissed", "boolean", nullable: false)
        }
    }

    void down() {
        dropTable("adverse_events");
        dropTable("ae_notifications");
    }
}
