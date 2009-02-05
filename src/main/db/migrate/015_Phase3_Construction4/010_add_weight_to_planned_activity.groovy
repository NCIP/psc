class AddWeightToPlannedActivity extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("planned_activities", "weight", 'integer', nullable: true)
        execute("update planned_activities SET weight=0");
    }

    void down() {
        dropColumn("planned_activities", "weight")
    }
}