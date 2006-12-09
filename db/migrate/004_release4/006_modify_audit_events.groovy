class ModifyAuditEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameColumn("audit_events", "user_name", "username");
        renameColumn("audit_events", "class_name", "object_class");
        renameColumn("audit_event_values", "new_value", "current_value");

        // existing version columns do not have proper settings
        setDefaultValue("audit_events", "version", "0");
        setDefaultValue("audit_event_values", "version", "0");

        // object id should be an int, not a string
        dropColumn("audit_events", "object_id")
        addColumn("audit_events", "object_id", "integer", nullable: false)
    }

    void down() {
        renameColumn("audit_events", "username", "user_name");
        renameColumn("audit_events", "object_class", "class_name");
        renameColumn("audit_event_values", "current_value", "new_value");

        dropColumn("audit_events", "object_id")
        addColumn("audit_events", "object_id", "string", nullable: false)
    }
}
