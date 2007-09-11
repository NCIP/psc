class ModifyAuditEvent extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        
        renameColumn("audit_events", "username", "user_name");
        renameColumn("audit_events", "object_class","class_name");
        renameColumn("audit_event_values", "current_value","new_value");
    }

    void down() {

        renameColumn("audit_events", "user_name", "username");
        renameColumn("audit_events", "class_name", "object_class");
        renameColumn("audit_event_values", "new_value", "current_value");

    }
}

