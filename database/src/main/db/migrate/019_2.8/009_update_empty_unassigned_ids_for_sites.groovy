class UpdateEmptyUnassignedIdForSite extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("UPDATE sites SET assigned_identifier=name WHERE assigned_identifier IS NULL");
        setNullable("sites", "assigned_identifier", false);
    }

    void down() {
        setNullable("sites", "assigned_identifier", true);    
    }
}