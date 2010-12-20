class AddUserActionToAuditEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("audit_events", "user_action_id", 'string', nullable:true, limit: 255)
    }

    void down() {
        dropColumn("audit_events", "user_action_id")
    }
}
