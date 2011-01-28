class CreateSubjectProperties extends edu.northwestern.bioinformatics.bering.Migration {
    @Override
    void up() {
        createTable("subject_properties") { t ->
            t.setIncludePrimaryKey(false);
            t.addColumn("subject_id", "integer", nullable: false);
            t.addColumn("list_index", "integer", defaultValue: 0, nullable: false)
            t.addColumn("name", "string", nullable: false);
            t.addColumn("value", "string");
        }

        execute("ALTER TABLE subject_properties ADD CONSTRAINT pk_subject_property PRIMARY KEY (subject_id, name)")
        // Combining table-defined and later-defined constraints with HSQLDB seems to cause naming
        // confusion, so we don't use bering's built-in FK support here.
        execute("ALTER TABLE subject_properties ADD CONSTRAINT fk_subject_prop_subject FOREIGN KEY (subject_id) REFERENCES subjects")
    }

    @Override
    void down() {
        dropTable("subject_properties");
    }
}
