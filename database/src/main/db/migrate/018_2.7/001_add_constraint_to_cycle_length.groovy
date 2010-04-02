class AddConstraintToCycleLength extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute('ALTER TABLE study_segments ADD CONSTRAINT check_cycle_length CHECK (cycle_length > 0)')
    }

    void down() {
        execute("ALTER TABLE study_segments DROP CONSTRAINT check_cycle_length")
    }
}