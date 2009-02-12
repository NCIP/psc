class AddLoadStatusToSubject extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("subjects", "load_status", "integer", nullable: true , defaultValue:"1");
        execute("update subjects set load_status = 1");
        setNullable("subjects", "load_status", false);
    }

    void down() {
        dropColumn("subjects", "load_status")
     }
 }

