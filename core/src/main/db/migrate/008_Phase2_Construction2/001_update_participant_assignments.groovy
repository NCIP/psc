class UpdateParticipantAssignments extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("participant_assignments", "participant_coordinator_id", "integer")
    }

    void down() {
        dropColumn("participant_assignments", "participant_coordinator_id")
    }
}