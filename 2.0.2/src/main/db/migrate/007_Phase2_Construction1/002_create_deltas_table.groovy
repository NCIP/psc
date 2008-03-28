class CreateDeltasTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("deltas") { t ->
            t.addVersionColumn()
            t.addColumn("discriminator_id", "integer", nullable:false)
            t.addColumn("change_id", "integer", nullable:false)
            t.addColumn("node_id", "integer", nullable: false)
            t.addColumn("amendment_id", "integer", nullable: false)
            t.addColumn("grid_id", "string", nullable: true)
        }
    }

    void down() {
        dropTable("deltas");
    }
}

