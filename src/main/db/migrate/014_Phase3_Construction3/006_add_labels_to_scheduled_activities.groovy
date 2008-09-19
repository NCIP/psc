class AddLabelsToScheduledActivities extends edu.northwestern.bioinformatics.bering.Migration {
    public void up() {
        createTable("scheduled_activity_labels") { t ->
            t.includePrimaryKey = false
            t.addColumn("scheduled_activity_id", "integer", nullable: false)
            t.addColumn("label", "string", nullable: false, limit: 255)
        }
        execute("ALTER TABLE scheduled_activity_labels ADD CONSTRAINT fk_sched_act_label_sched_act FOREIGN KEY (scheduled_activity_id) REFERENCES scheduled_activities");
        execute("ALTER TABLE scheduled_activity_labels ADD CONSTRAINT pk_sched_act_label PRIMARY KEY (scheduled_activity_id, label)");
    }

    public void down() {
        dropTable("scheduled_activity_labels", primaryKey: false)
    }
}