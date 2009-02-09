class AddWeightToPlannedActivity extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("planned_activities", "weight", 'integer', nullable: true)
    }

    void down() {
        dropColumn("planned_activities", "weight")
    }
}