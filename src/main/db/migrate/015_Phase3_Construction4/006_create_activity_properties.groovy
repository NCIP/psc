class CreateActivityProperties extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("activity_properties") { t ->
            t.addVersionColumn()
            t.addColumn("activity_id", "integer", nullable: false)
            t.addColumn("namespace", "string", nullable:false)
            t.addColumn("name", "string", nullable: false)
            t.addColumn("value", "string", nullable:true)
            t.addColumn("grid_id", "string", limit: 255)
        }
        execute("ALTER TABLE activity_properties ADD CONSTRAINT un_namespace_name_activity UNIQUE (activity_id, namespace, name)");
        execute("ALTER TABLE activity_properties ADD CONSTRAINT fk_act_property_act FOREIGN KEY (activity_id) REFERENCES activities");
    }

    void down() {
        dropTable("activity_properties");
    }
}