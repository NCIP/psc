class AddSecondaryStudyIdentifiers extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("study_secondary_idents")  { t ->
            t.addVersionColumn();
            t.addColumn("grid_id", "string", limit: 255);
            t.addColumn("study_id", "integer", nullable: false);
            t.addColumn("identifier_type", "string", limit: 255, nullable: false);
            t.addColumn("value", "string", limit: 255, nullable: false);
        }
        execute("ALTER TABLE study_secondary_idents ADD CONSTRAINT fk_study_sec_ident FOREIGN KEY (study_id) REFERENCES studies");
    }

    void down() {
        removeTable("study_secondary_idents");
    }
}