class TrackScheduledEventSourceRepetition extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("scheduled_events", "repetition_number", "integer");
    }

    void down() {
        dropColumn("scheduled_events", "repetition_number");
    }
}
