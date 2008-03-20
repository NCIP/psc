class RenameParticipantsToSubjects extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("participants", "subjects")
        renameTable("participant_assignments", "subject_assignments")
        renameColumn("subject_assignments", "participant_coordinator_id", "subject_coordinator_id")
        renameColumn("subject_assignments", "participant_id", "subject_id")
    }

    void down() {
        renameTable("subjects", "participants")
        renameTable("subject_assignments", "participant_assignments")
        renameColumn("participant_assignments", "subject_coordinator_id", "participant_coordinator_id")
        renameColumn("participant_assignments", "subject_id", "participant_id")
    }
}