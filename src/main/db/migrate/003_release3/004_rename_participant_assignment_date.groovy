class RenameParticipantAssignmentDate extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameColumn("participant_assignments", "date_of_enrollment", "first_epoch_stdate");
    }

    void down() {
        renameColumn("participant_assignments", "first_epoch_stdate", "date_of_enrollment");
    }
}