class AddUniqueConstraintToLabelTables extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE labels ADD CONSTRAINT un_label_name UNIQUE (name)")
        execute("ALTER TABLE planned_activity_labels ADD CONSTRAINT fk_label_id FOREIGN KEY (label_id) REFERENCES labels")
        execute("ALTER TABLE planned_activity_labels ADD CONSTRAINT un_label_pa_rep_num UNIQUE (planned_activity_id, label_id, rep_num)")
    }

    void down() {
        execute("ALTER TABLE planned_activity_labels DROP CONSTRAINT un_label_pa_rep_num")
        execute("ALTER TABLE planned_activity_labels DROP CONSTRAINT fk_label_id")
        execute("ALTER TABLE labels DROP CONSTRAINT un_label_name")
    }
}