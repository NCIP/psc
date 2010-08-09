class RemoveOldAssignmentSubjectCoordinatorLink extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn('subject_assignments', 'manager_csm_user_id', 'integer');
        execute("UPDATE subject_assignments ssa SET manager_csm_user_id=(SELECT u.csm_user_id FROM users u WHERE ssa.subject_coordinator_id=u.id)")
        dropColumn('subject_assignments', 'subject_coordinator_id');
    }

    void down() {
        addColumn('subject_assignments', 'subject_coordinator_id', 'integer');
        execute("UPDATE subject_assignments ssa SET subject_coordinator_id=(SELECT u.id FROM users u WHERE ssa.manager_csm_user_id=u.csm_user_id)")
        dropColumn('subject_assignments', 'manager_csm_user_id');
    }
}
