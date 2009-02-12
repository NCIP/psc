class UpdateStudyAmendmentRelationships extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        // reverse responsibility for the study -> current amendment link
        dropColumn("amendments", "study_id")
        addColumn("studies", "amendment_id", "integer")

        // add study -> in-development amendment link.  "development_amendment_id" is too long.
        addColumn("studies", "dev_amendment_id", "integer")

        renameColumn("amendments", "previous_amendment", "previous_amendment_id")

        if (databaseMatches("hsqldb")) {
            // HSQLDB doesn't correctly order DELETEs when clearing a table with self-references
            // so we need to cascade this constraint.  Don't want to generally, though.
            execute("ALTER TABLE amendments ADD CONSTRAINT fk_amendment_prev FOREIGN KEY (previous_amendment_id) REFERENCES amendments ON DELETE CASCADE")
        } else {
            execute("ALTER TABLE amendments ADD CONSTRAINT fk_amendment_prev FOREIGN KEY (previous_amendment_id) REFERENCES amendments")
        }
        execute("ALTER TABLE studies ADD CONSTRAINT fk_study_cur_amendment FOREIGN KEY (amendment_id) REFERENCES amendments")
        execute("ALTER TABLE studies ADD CONSTRAINT fk_study_dev_amendment FOREIGN KEY (dev_amendment_id) REFERENCES amendments")
    }

    void down() {
        renameColumn("amendments", "previous_amendment_id", "previous_amendment")
        dropColumn("studies", "dev_amendment_id")
        addColumn("amendments", "study_id", "integer")
        dropColumn("studies", "amendment_id")

        execute("ALTER TABLE amendments DROP CONSTRAINT fk_amendment_prev");
        execute("ALTER TABLE studies DROP CONSTRAINT fk_study_cur_amendment");
        execute("ALTER TABLE studies DROP CONSTRAINT fk_study_dev_amendment");
    }
}
