class ExpandAuditEventValues extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        if (databaseMatches("postgresql")) {
            execute("ALTER TABLE audit_event_values ALTER previous_value TYPE TEXT");
            execute("ALTER TABLE audit_event_values ALTER current_value TYPE TEXT");
        } else if (databaseMatches("oracle")) {
            execute("ALTER TABLE audit_event_values MODIFY (previous_value VARCHAR2(4000), current_value VARCHAR2(4000))");
        }
    }

    void down() {
         // don't bother
    }
}