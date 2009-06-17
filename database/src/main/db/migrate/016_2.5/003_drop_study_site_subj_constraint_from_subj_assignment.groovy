class DropStudySiteSubjConstraintFromSubjAssignment extends edu.northwestern.bioinformatics.bering.Migration {
  void up() {
    execute("ALTER TABLE subject_assignments DROP CONSTRAINT un_study_site_subject")
  }
  void down() {
    execute("ALTER TABLE subject_assignments ADD CONSTRAINT un_study_site_subject UNIQUE (study_site_id, subject_id)")
  }
}



