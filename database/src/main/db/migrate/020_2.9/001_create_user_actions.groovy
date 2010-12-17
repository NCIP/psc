class CreateUserActions extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("user_actions") { t ->
            t.addVersionColumn()
            t.addColumn("csm_user_id", "integer", nullable: false)
            t.addColumn("context", "string", nullable:false)
            t.addColumn("description", "string", nullable:false)
            t.addColumn("action_type", "string", nullable:false)
            t.addColumn("undone", "boolean", defaultValue:"0")
            t.addColumn("grid_id", "string", limit: 255)
        }
    }

    void down() {
        dropTable("user_actions");
    }
}