--
-- This script marks any ScheduledEvent (for any participant) as "occurred" if its
-- date is before "filter_date" (set below).
--
-- n.b., this script:
--   ... will only work with psql
--   ... bypasses the PSC's application-level database update auditing

BEGIN TRANSACTION;

-- The date before which to mark events occurred
\set filter_date 'DATE \'2007-03-08\''

-- Move the current state to the history
INSERT INTO scheduled_event_states (scheduled_event_id, mode_id, actual_date, reason, list_index)
  SELECT id, current_state_mode_id, current_state_date, current_state_reason,
    COALESCE(
      (SELECT MAX(list_index) + 1 FROM scheduled_event_states ses WHERE ses.scheduled_event_id=se.id), 0
    )
  FROM scheduled_events se WHERE se.current_state_mode_id=1 AND se.current_state_date < :filter_date;

-- Update with new mode & reason
UPDATE scheduled_events SET current_state_mode_id=2, current_state_reason='Batch change: demo data'
  WHERE current_state_mode_id=1 AND current_state_date < :filter_date;

COMMIT;