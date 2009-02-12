class CreateLabel extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("labels") { t ->
            t.addVersionColumn()
            t.addColumn("name", "string", nullable: false)
            t.addColumn("grid_id", "string", limit: 255)
        }
    }

    void down() {
        dropTable("labels");
    }
}