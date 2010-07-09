-- This down script reverts enough of the changes to CSM that the corresponding
-- up script can be run again.  It does not fully revert to CSM 3.2.

DROP TABLE CSM_FILTER_CLAUSE;
DROP SEQUENCE CSM_FILTER_CLAUSE_FILTE_ID_SEQ;

DROP TABLE CSM_MAPPING;
DROP SEQUENCE CSM_MAPPING_MAPPING_ID_SEQ;
