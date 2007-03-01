--
-- This script marks any ScheduledEvent (for any participant) as "occurred" if its
-- date is before March 8, 2007.
--
-- Note that this script will only work on a PostgreSQL-backed PSC deployment.  It lacks the
-- explicit sequence refs that Oracle requires.
--
-- Note also that this script bypasses the PSC's application-level database update auditing.

BEGIN TRANSACTION;

-- Move the current state to the history
INSERT INTO scheduled_event_states (scheduled_event_id, mode_id, actual_date, reason, list_index)
  SELECT id, current_state_mode_id, current_state_date, current_state_reason,
    COALESCE(
      (SELECT MAX(list_index) + 1 FROM scheduled_event_states ses WHERE ses.scheduled_event_id=se.id), 0
    )
  FROM scheduled_events se WHERE se.current_state_mode_id=1 AND se.current_state_date < DATE '2007-03-08';

-- Update with new mode & reason
UPDATE scheduled_events SET current_state_mode_id=2, current_state_reason='Batch changed for demo'
  WHERE current_state_mode_id=1 AND current_state_date < DATE '2007-03-08';

COMMIT;