class DropNotNullFromActivities extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        setNullable("activities", "description", true);
    }

    void down() {
        setNullable("activities", "description", false);
    }
}