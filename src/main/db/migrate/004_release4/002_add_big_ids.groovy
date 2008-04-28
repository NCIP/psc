class AddBigIds extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "big_id", "string", limit: 255)
        addColumn("sites", "big_id", "string", limit: 255)
        addColumn("participants", "big_id", "string", limit: 255)
        addColumn("scheduled_events", "big_id", "string", limit: 255)
        addColumn("arms", "big_id", "string", limit: 255)
    }

    void down() {
        dropColumn("studies", "big_id")
        dropColumn("sites", "big_id")
        dropColumn("participants", "big_id")
        dropColumn("scheduled_events", "big_id")
        dropColumn("arms", "big_id")
    }
}
