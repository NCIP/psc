class ModifyScheduledEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("scheduled_events", "activity_id", "integer")
        addColumn("scheduled_events", "details", "string")
        setNullable("scheduled_events", "planned_event_id", true)
        insert("activities", [ activity_type_id: 5, name: "Reconsent" ])
    }

    void down() {
        dropColumn("scheduled_events", "activity_id")
        dropColumn("scheduled_events", "details")
        setNullable("scheduled_events", "planned_event_id", false)
        execute("DELETE FROM activities where name = 'Reconsent'")
    }
}