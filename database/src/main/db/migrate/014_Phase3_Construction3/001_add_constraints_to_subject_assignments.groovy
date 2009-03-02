class AddConstraintsToSubjectAssignments extends edu.northwestern.bioinformatics.bering.Migration {
	void up() {
		execute('ALTER TABLE subject_assignments ADD CONSTRAINT fk_subjectassign_subject FOREIGN KEY (subject_id) REFERENCES subjects')
	}
	void down() {
        execute("ALTER TABLE subjects DROP CONSTRAINT fk_subjectassign_subject")
	}
}

