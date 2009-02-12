///// It appears that hibernate expects the ID sequence for a PostgreSQL SERIAL column to be
///// named in the default way, so we need to rename sequence for planned_calendars
class RenamePostgresqlPlannedScheduleArtifacts extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
//        if (databaseMatches('postgresql')) {
            // this is weird, but it works
//            execute("ALTER TABLE planned_schedules_id_seq RENAME TO planned_calendars_id_seq");
//        }
    }

    void down() {
//        if (databaseMatches('postgresql')) {
//            execute("ALTER TABLE planned_calendars_id_seq RENAME TO planned_schedules_id_seq");
//        }
    }
}
