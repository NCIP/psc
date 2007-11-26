class RenameArmsTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("arms", "study_segments")
        renameTable("scheduled_arms", "scheduled_study_segments")
        renameColumn("scheduled_study_segments", "arm_id", "study_segment_id")
        renameColumn("periods", "arm_id", "study_segment_id")
        renameColumn("scheduled_activities", "scheduled_arm_id", "scheduled_study_segment_id")

        execute("UPDATE delta_node_types SET node_type='studysegment' WHERE node_type='arm'");
        execute("UPDATE delta_node_types SET node_table='study_segments' WHERE node_table='arms'");

        execute("UPDATE deltas SET node_type='studysegment' WHERE node_type='arm'");

    }

    void down() {
        renameTable("study_segments", "arms")
        renameTable("scheduled_study_segments", "scheduled_arms")
        renameColumn("scheduled_arms", "study_segment_id", "arm_id")
        renameColumn("periods", "study_segment_id", "arm_id")
        renameColumn("scheduled_activities", "scheduled_study_segment_id", "scheduled_arm_id")

        execute("UPDATE delta_node_types SET node_type='arm' WHERE node_type='studysegment'");
        execute("UPDATE delta_node_types SET node_table='arms' WHERE node_table='study_segments'");

        execute("UPDATE deltas SET node_type='arm' WHERE node_type='studysegment'");
    }
}