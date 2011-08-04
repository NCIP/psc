class AddTimeFlagToScheduledActivityState extends edu.northwestern.bioinformatics.bering.Migration {
    @Override
    void up() {
        addColumn("scheduled_activity_states", "with_time", "boolean", nullable: false, defaultValue:"0")
        addColumn("scheduled_activities", "current_state_with_time", "boolean", nullable: false, defaultValue:"0")
    }

    @Override
    void down() {
        removeColumn("scheduled_activity_states", "with_time");
        removeColumn("scheduled_activities", "current_state_with_time");
    }
}