class AddDeltaTypesForPopulation extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("insert into delta_node_types (node_type, node_table) values ('study', 'populations')");
        execute("insert into delta_node_types (node_type, node_table) values ('popltn', 'populations')");
        setNullable("populations", "study_id", true)
    }
    void down() {
        execute("delete from delta_node_types where node_type='study' ");
        execute("delete from delta_node_types where node_type='popltn' ");
        setNullable("populations", "study_id", false)
    }
}