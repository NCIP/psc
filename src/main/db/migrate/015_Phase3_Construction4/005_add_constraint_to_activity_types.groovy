class AddConstraintToActivityTypes extends edu.northwestern.bioinformatics.bering.Migration {
	void up() {
		execute("ALTER TABLE activity_types ADD CONSTRAINT un_type_name UNIQUE (name)")
	}
	void down() {
        execute("ALTER TABLE activity_types DROP CONSTRAINT un_type_name")
	}
}