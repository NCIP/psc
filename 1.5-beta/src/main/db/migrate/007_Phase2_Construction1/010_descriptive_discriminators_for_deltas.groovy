class DescriptiveDiscriminatorsForDeltas extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // constraint table
        createTable("delta_node_types") { t ->
            t.setIncludePrimaryKey(false)
            t.addColumn("node_type", "string", primaryKey: true, limit: 6)
            t.addColumn("node_table", "string", nullable: false)
        }
        insert('delta_node_types', [node_type: 'cal',    node_table: 'planned_calendars'], primaryKey: false)
        insert('delta_node_types', [node_type: 'epoch',  node_table: 'epochs'],            primaryKey: false)
        insert('delta_node_types', [node_type: 'arm',    node_table: 'arms'],              primaryKey: false)
        insert('delta_node_types', [node_type: 'period', node_table: 'periods'],           primaryKey: false)
        insert('delta_node_types', [node_type: 'event',  node_table: 'planned_events'],    primaryKey: false)

        // new discriminator
        addColumn("deltas", "node_type", "string", limit: 6)
        execute("ALTER TABLE deltas ADD CONSTRAINT fk_delta_node_type FOREIGN KEY (node_type) REFERENCES delta_node_types");
        execute("UPDATE deltas SET node_type='epoch' WHERE discriminator_id=1");
        execute("UPDATE deltas SET node_type='cal' WHERE discriminator_id=2");
        setNullable("deltas", "node_type", false)

        dropColumn("deltas", "discriminator_id")
    }

    void down() {
        addColumn("deltas", "discriminator_id", "integer")
        execute("UPDATE deltas SET discriminator_id=1 WHERE node_type='epoch'")
        execute("UPDATE deltas SET discriminator_id=2 WHERE node_type='cal'")
        // these are theoretical
        execute("UPDATE deltas SET discriminator_id=3 WHERE node_type='arm'")
        execute("UPDATE deltas SET discriminator_id=4 WHERE node_type='period'")
        execute("UPDATE deltas SET discriminator_id=5 WHERE node_type='event'")
        dropColumn("deltas", "node_type")

        dropTable("delta_node_types", primaryKey: false)
    }
}