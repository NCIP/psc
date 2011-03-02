class UpdateSubjectPropertiesConstraint extends edu.northwestern.bioinformatics.bering.Migration {
    @Override
    void up() {
        execute("ALTER TABLE subject_properties DROP CONSTRAINT pk_subject_property");
        execute("ALTER TABLE subject_properties DROP CONSTRAINT fk_subject_prop_subject");
        execute("ALTER TABLE subject_properties ADD CONSTRAINT pk_subject_property PRIMARY KEY (subject_id, list_index)")
        execute("ALTER TABLE subject_properties ADD CONSTRAINT fk_subject_prop_subject FOREIGN KEY (subject_id) REFERENCES subjects")
    }

    @Override
    void down() {
        execute("ALTER TABLE subject_properties DROP CONSTRAINT pk_subject_property");
        execute("ALTER TABLE subject_properties ADD CONSTRAINT pk_subject_property PRIMARY KEY (subject_id, name)")
        execute("ALTER TABLE subject_properties DROP CONSTRAINT fk_subject_prop_subject");
        execute("ALTER TABLE subject_properties ADD CONSTRAINT fk_subject_prop_subject FOREIGN KEY (subject_id) REFERENCES subjects")
    }
}
