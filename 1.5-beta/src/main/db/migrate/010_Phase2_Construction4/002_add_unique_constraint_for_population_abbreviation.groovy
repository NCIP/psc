class AddUniqueConstraintForPopulationAbbreviation extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("ALTER TABLE populations ADD CONSTRAINT un_pop_abbrev_study UNIQUE (abbreviation, study_id)")
    }

    void down() {
        execute("ALTER TABLE populations DROP CONSTRAINT un_pop_abbrev_study")
    }
}
