class AddLastEpochEndDate extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("participant_assignments", "last_epoch_enddate", "date")
    }

    void down() {
        dropColumn("participant_assignments", "last_epoch_enddate")
    }
}
