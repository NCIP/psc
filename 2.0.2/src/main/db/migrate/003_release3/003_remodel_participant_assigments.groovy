//// Participant_assignments refers to both studies and study_sites.  Drop redundant studies ref.
class RemodelParticipantAssignments extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        removeColumn("participant_assignments", "study_id");
    }

    void down() {
        // Note that this is not a pure reversal, as it does not restore the existing (redundant) data
        // and therefore does not restore the NOT NULL & fk constraints.
        addColumn("participant_assignments", "study_id", "integer");
    }
}