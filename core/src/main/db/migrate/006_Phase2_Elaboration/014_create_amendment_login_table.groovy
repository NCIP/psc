class CreateAmendmentLoginTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("amendment_logins") { t ->
            t.addVersionColumn()
            t.addColumn("study_id", "integer", nullable: false)
            t.addColumn("amendment_number", "integer", nullable: false)
            t.addColumn("amendment_date", "string", nullable: false)
            t.addColumn("grid_id", "string", nullable: true, limit: 255)
        }
    }

    void down() {
        dropTable("amendment_logins");
    }
}
