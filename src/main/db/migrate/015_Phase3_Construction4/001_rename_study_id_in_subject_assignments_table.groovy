class RenameStudyIdInStudyAssignmentsTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameColumn("subject_assignments", "study_id", "study_subject_id");
    }

    void down() {
        renameColumn("subject_assignments", "study_subject_id", "study_id");
    }
}