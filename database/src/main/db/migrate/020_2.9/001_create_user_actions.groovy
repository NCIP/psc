class CreateUserActions extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("user_actions") { t ->
            t.addVersionColumn()
            t.addColumn("user_id", "integer", nullable: false)
            t.addColumn("uri", "string", nullable:false)
            t.addColumn("description", "string", nullable:false)
            t.addColumn("action_type", "string", nullable:false)
            t.addColumn("undone", "boolean", nullable:false, defaultValue:false)
            t.addColumn("grid_id", "string", limit: 255)
        }
        execute("ALTER TABLE user_actions ADD CONSTRAINT fk_user_actions_users FOREIGN KEY (user_id) REFERENCES users");
    }

    void down() {
        dropTable("user_actions");
    }
}