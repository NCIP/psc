/**
 * Standardize domain objects so that they match the interfaces & abstract base classes
 * in ctms-commons.  In particular
 *  - Rename bigId -> gridId
 *  - Add gridId to all mutable domain objects that don't have it
 */
public class StandardizeDomainObjects extends edu.northwestern.bioinformatics.bering.Migration {
    String[] WITH_GRID_ID = ['adverse_events', 'arms', 'participants', 'scheduled_events', 'sites',
        'studies', 'participant_assignments']
    String[] NEED_GRID_ID = ['activities', 'ae_notifications', 'epochs', 'holidays',
        'periods', 'planned_calendars', 'planned_events', 'scheduled_arms', 'scheduled_calendars',
        'study_sites', 'users', 'audit_events', 'audit_event_values', 'login_audits',
        'scheduled_event_states']

    void up() {
        WITH_GRID_ID.each { table ->
            renameColumn(table, 'big_id', 'grid_id')
        }

        NEED_GRID_ID.each { table ->
            addColumn(table, 'grid_id', 'string', limit: 255)
            if (databaseMatches('sqlserver'))
                execute("UPDATE ${table} SET grid_id='${java.util.UUID.randomUUID().toString()}---conv:' + Str(id)")
            else
                execute("UPDATE ${table} SET grid_id='${java.util.UUID.randomUUID().toString()}---conv:' || id")
        }
    }

    void down() {
        NEED_GRID_ID.each { table ->
            dropColumn(table, 'grid_id');
        }

        WITH_GRID_ID.each { table ->
            renameColumn(table, 'grid_id', 'big_id')
        }
    }
}
