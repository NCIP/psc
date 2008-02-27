class DropSubjectConstraints extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setNullable("subjects", "first_name", true);
        setNullable("subjects", "last_name", true);
        setNullable("subjects", "birth_date", true);
        setNullable("subjects", "person_id", true);
    }

    void down() {
        setNullable("subjects", "first_name", false);
        setNullable("subjects", "last_name", false);
        setNullable("subjects", "birth_date", false);
        setNullable("subjects", "person_id", false);
     }
 }

