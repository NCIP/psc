class AddSubjectConstraints extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE subjects ADD CONSTRAINT ck_identifiers  \
                    CHECK (person_id IS NOT NULL                     \
                    or (person_id IS NULL and first_name IS NOT NULL and last_name IS NOT NULL and birth_date IS NOT NULL))")
    }

    void down() {
        execute("ALTER TABLE subjects DROP CONSTRAINT ck_identifiers")
    }
}