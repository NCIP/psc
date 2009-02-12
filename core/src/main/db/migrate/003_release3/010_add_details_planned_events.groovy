class AddDetailsPlannedEvents extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
    	addColumn('planned_events', 'details', 'string');
    }

    void down() {
    	removeColumn('planned_events', 'details');
    }

}