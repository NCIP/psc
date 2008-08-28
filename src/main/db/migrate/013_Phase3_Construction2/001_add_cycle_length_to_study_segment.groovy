class AddCycleLengthToStudySegment extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("study_segments", "cycle_length", 'integer',nullable: true)
    }

    void down() {
        dropColumn("study_segments", "cycle_length")
    }
}