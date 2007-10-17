class AddBigIdToSpa extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("participant_assignments", "big_id", "string")
    }

    void down() {
        dropColumn("participant_assignments", "big_id")
    }
}