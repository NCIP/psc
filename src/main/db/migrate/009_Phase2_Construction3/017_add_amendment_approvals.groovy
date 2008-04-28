class AddAmendmentApprovals extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("amendment_approvals") { t ->
            t.addVersionColumn()
            t.addColumn("grid_id", "string", limit: 255)
            t.addColumn("amendment_id", "integer", nullable: false)
            t.addColumn("study_site_id", "integer", nullable: false)
            t.addColumn("approval_date", "date")
        }

        execute("ALTER TABLE amendment_approvals ADD CONSTRAINT fk_apprv_amendt FOREIGN KEY (amendment_id) REFERENCES amendments");
        execute("ALTER TABLE amendment_approvals ADD CONSTRAINT fk_apprv_study_site FOREIGN KEY (study_site_id) REFERENCES study_sites");
    }

    void down() {
        dropTable("amendment_approvals");
    }
}
