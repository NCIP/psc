// Constraint table for the discriminator column in CHANGES
class ChangeActionLookup extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        createTable("change_actions") { t ->
            t.setIncludePrimaryKey(false)
            t.addColumn("action", "string", primaryKey: true, limit: 8)
        }

        insert("change_actions", [ action: "add" ],      primaryKey: false)
        insert("change_actions", [ action: "remove" ],   primaryKey: false)
        insert("change_actions", [ action: "reorder" ],  primaryKey: false)
        insert("change_actions", [ action: "property" ], primaryKey: false)

        execute("ALTER TABLE changes ADD CONSTRAINT fk_change_action FOREIGN KEY (action) REFERENCES change_actions")
    }

    void down() {
        execute("ALTER TABLE changes DROP CONSTRAINT fk_change_action")
        dropTable("change_actions")
    }
}