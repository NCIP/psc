class AddLabelDeltaType extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("INSERT INTO delta_node_types (node_type, node_table) VALUES ('label', 'planned_activity_labels')");
    }

    void down() {
        execute("DELETE FROM delta_node_types WHERE node_type='label'");
    }
}