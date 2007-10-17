class AddBigIds extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "big_id", "string")
        addColumn("sites", "big_id", "string")
        addColumn("participants", "big_id", "string")
        addColumn("scheduled_events", "big_id", "string")
        addColumn("arms", "big_id", "string")
    }

    void down() {
        dropColumn("studies", "big_id")
        dropColumn("sites", "big_id")
        dropColumn("participants", "big_id")
        dropColumn("scheduled_events", "big_id")
        dropColumn("arms", "big_id")
    }
}