class AddAuditEventUserActionConstraint extends edu.northwestern.bioinformatics.bering.Migration {
    @Override
    void up() {
        execute("ALTER TABLE user_actions ADD CONSTRAINT un_user_action_grid_id UNIQUE (grid_id)");
        execute("ALTER TABLE audit_events ADD CONSTRAINT fk_audit_event_user_action FOREIGN KEY (user_action_id) REFERENCES user_actions(grid_id)")
    }

    @Override
    void down() {
        execute("ALTER TABLE audit_events DROP CONSTRAINT fk_audit_event_user_action");
        execute("ALTER TABLE user_actions DROP CONSTRAINT un_user_action_grid_id");
    }
}
