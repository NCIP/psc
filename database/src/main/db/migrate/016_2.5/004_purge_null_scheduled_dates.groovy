// This is a repeat of 14-3 because, even after we added dates to all modes, there
// was still code which was sometimes setting the dates to null.  It also adds
// a NOT NULL constraint to the relevant fields to prevent this from happening again.
class PurgeNullScheduledDates extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("update scheduled_activity_states sas1 set actual_date = (select actual_date from scheduled_activity_states sas2 where sas2.list_index = (select max(list_index) from scheduled_activity_states sas3 where sas3.list_index < sas1.list_index and sas3.scheduled_activity_id = sas1.scheduled_activity_id and sas3.actual_date is not null) and sas2.scheduled_activity_id = sas1.scheduled_activity_id ) where actual_date is null")
        execute("update scheduled_activities sa set current_state_date = (select actual_date from scheduled_activity_states sas1 where list_index = (select max(list_index) from scheduled_activity_states sas2 where sas2.scheduled_activity_id = sas1.scheduled_activity_id) and sas1.scheduled_activity_id = sa.id) where current_state_date is null")
        setNullable("scheduled_activities", "current_state_date", false);
        setNullable("scheduled_activity_states", "actual_date", false);
    }

    void down() {
    }
}