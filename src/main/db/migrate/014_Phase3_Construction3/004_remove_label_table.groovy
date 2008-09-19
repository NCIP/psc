class RemoveLabelTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("planned_activity_labels", "label", "string", limit: 255)
        execute("UPDATE planned_activity_labels SET label=(SELECT name FROM labels WHERE id=label_id)")
        execute("ALTER TABLE planned_activity_labels DROP CONSTRAINT un_label_pa_rep_num")
        execute("ALTER TABLE planned_activity_labels ADD CONSTRAINT un_label_pa_rep_num UNIQUE (planned_activity_id, label, rep_num)")

        execute("ALTER TABLE planned_activity_labels DROP CONSTRAINT fk_label_id")
        removeColumn("planned_activity_labels", "label_id")
        dropTable("labels")
    }

    void down() {
        // there are no production systems with labels in them so we will revert the structure only
        execute("DELETE FROM planned_activity_labels")

        createTable("labels") { t ->
            t.addVersionColumn()
            t.addColumn("name", "string", nullable: false)
            t.addColumn("grid_id", "string", limit: 255)
        }
        execute("ALTER TABLE labels ADD CONSTRAINT un_label_name UNIQUE (name)")

        addColumn("planned_activity_labels", "label_id", "integer")
        execute("ALTER TABLE planned_activity_labels ADD CONSTRAINT fk_label_id FOREIGN KEY (label_id) REFERENCES labels")
        execute("ALTER TABLE planned_activity_labels DROP CONSTRAINT un_label_pa_rep_num")
        execute("ALTER TABLE planned_activity_labels ADD CONSTRAINT un_label_pa_rep_num UNIQUE (planned_activity_id, label_id, rep_num)")
        removeColumn("planned_activity_labels", "label")
    }
}