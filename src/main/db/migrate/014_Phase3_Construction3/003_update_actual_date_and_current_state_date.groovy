class UpdateActualDateAndCurrentStateDate extends edu.northwestern.bioinformatics.bering.Migration {

  void up() {

        execute("update scheduled_activity_states as sas1 set actual_date = (select actual_date from scheduled_activity_states as sas2 where sas2.list_index = (select max(list_index) from scheduled_activity_states as sas3 where sas3.list_index < sas1.list_index and sas3.scheduled_activity_id = sas1.scheduled_activity_id and sas3.actual_date is not null) and sas2.scheduled_activity_id = sas1.scheduled_activity_id ) where actual_date is null")
        execute("update scheduled_activities as sa set current_state_date = (select actual_date from scheduled_activity_states as sas1 where list_index =  (select max(list_index) from scheduled_activity_states as sas2 where sas2.scheduled_activity_id = sas1.scheduled_activity_id ) and sas1.scheduled_activity_id = sa.id) where current_state_date is null")

  }
  void down() {

  }
 
}