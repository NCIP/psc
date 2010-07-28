class RenameAssignmentBoundaryDates extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameColumn('subject_assignments', 'first_epoch_stdate', 'start_date');
        renameColumn('subject_assignments', 'last_epoch_enddate', 'end_date'  );
    }

    void down() {
        renameColumn('subject_assignments', 'start_date', 'first_epoch_stdate');
        renameColumn('subject_assignments', 'end_date',   'last_epoch_enddate');
    }
}
