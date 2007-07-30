class CopyActivtyIdsAndDetails extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {     
        if (databaseMatches('postgresql')) {
            // postgresql's UPDATE from SELECT is not ANSI-compatible
            execute('UPDATE scheduled_events SET activity_id=p.activity_id FROM planned_events p INNER JOIN scheduled_events s ON s.planned_event_id=p.id where scheduled_events.id = s.id')
            execute('UPDATE scheduled_events SET details=p.details FROM planned_events p INNER JOIN scheduled_events s ON s.planned_event_id=p.id where scheduled_events.id = s.id')
        } else {
            execute('UPDATE scheduled_events SET activity_id=(SELECT p.activity_id FROM planned_events p INNER JOIN scheduled_events s ON s.planned_event_id=p._id where scheduled_events.id=s.id)')
            execute('UPDATE scheduled_events SET details=(SELECT p.details FROM planned_events p INNER JOIN scheduled_events s ON s.planned_event_id=p._id where scheduled_events.id=s.id)')
        }
    }

    void down() {
        execute('update scheduled_events set activity_id=NULL')
        execute('update scheduled_events set details=NULL')
    }
}