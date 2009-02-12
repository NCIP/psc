class CreatePlannedActivityLabels extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("planned_activity_labels") { t ->
            t.addVersionColumn()
            t.addColumn("planned_activity_id", "integer", nullable: false)
            t.addColumn("rep_num", "integer", nullable: false)
            t.addColumn("label_id", "integer", nullable: false)
            t.addColumn("grid_id", "string", limit: 255)
        }
    }

    void down() {
        dropTable("planned_activity_labels");
    }
}