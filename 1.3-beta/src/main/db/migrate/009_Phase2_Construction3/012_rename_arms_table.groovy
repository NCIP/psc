class RenameArmsTable extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        renameTable("arms", "study_segments")
        renameTable("scheduled_arms", "scheduled_study_segments")
        renameColumn("scheduled_study_segments", "arm_id", "study_segment_id")
        renameColumn("periods", "arm_id", "study_segment_id")
        renameColumn("scheduled_activities", "scheduled_arm_id", "scheduled_study_segment_id")

        // Changing the delta_node_types TLU will violate an FK no matter which order we do it,
        // so defer checking until commit.
        execute("ALTER TABLE deltas DROP CONSTRAINT fk_delta_node_type");

        execute("UPDATE delta_node_types SET node_type='segmnt' WHERE node_type='arm'");
        execute("UPDATE delta_node_types SET node_table='study_segments' WHERE node_table='arms'");
        execute("UPDATE deltas SET node_type='segmnt' WHERE node_type='arm'");

        // Reenable checking
        execute("ALTER TABLE deltas ADD CONSTRAINT fk_delta_node_type FOREIGN KEY (node_type) REFERENCES delta_node_types");
    }

    void down() {
        renameTable("study_segments", "arms")
        renameTable("scheduled_study_segments", "scheduled_arms")
        renameColumn("scheduled_arms", "study_segment_id", "arm_id")
        renameColumn("periods", "study_segment_id", "arm_id")
        renameColumn("scheduled_activities", "scheduled_study_segment_id", "scheduled_arm_id")

        // Changing the delta_node_types TLU will violate an FK no matter which order we do it,
        // so defer checking until commit.
        execute("ALTER TABLE deltas DROP CONSTRAINT fk_delta_node_type");

        execute("UPDATE delta_node_types SET node_type='arm' WHERE node_type='segmnt'");
        execute("UPDATE delta_node_types SET node_table='arms' WHERE node_table='study_segments'");
        execute("UPDATE deltas SET node_type='arm' WHERE node_type='segmnt'");

        // Reenable checking
        execute("ALTER TABLE deltas ADD CONSTRAINT fk_delta_node_type FOREIGN KEY (node_type) REFERENCES delta_node_types");
    }
}