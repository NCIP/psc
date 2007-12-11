--
-- PSC data for CTMSi demo
-- Demo data only -- all migrations and the base CSM config must also be applied
--
-- N.b.  This file's format (in particular the COPY statements) can only be interpreted by psql

BEGIN TRANSACTION;

-- Clear existing calendars, etc.
DELETE FROM scheduled_event_states;
DELETE FROM scheduled_events;
DELETE FROM scheduled_arms;
DELETE FROM scheduled_calendars;
DELETE FROM planned_events;
DELETE FROM periods;
DELETE FROM arms;
DELETE FROM epochs;
DELETE FROM planned_calendars;
DELETE FROM studies;
DELETE FROM configuration;

DELETE FROM activities WHERE id > 55;
COPY activities (id, version, name, description, activity_type_id) FROM stdin;
56	0	LMB-2		2
57	0	Lipid Panel		3
58	0	Chem 20		3
59	0	Creatinine		3
\.


COPY studies (id, name, version, big_id, protocol_authority_id) FROM stdin;
1	LMB-2 Immunotoxin in Treating Patients With Chronic Lymphocytic Leukemia or Prolymphocytic Leukemia	1	b13b207a-2289-4350-8883-25492fc0e8ea	04_C_0121
\.


COPY planned_calendars (id, study_id, complete, version) FROM stdin;
1	1	t	2
\.


COPY epochs (id, version, name, planned_calendar_id, list_index) FROM stdin;
2	2	Treatment	1	0
3	0	Follow up	1	1
\.


COPY arms (id, version, name, epoch_id, list_index, big_id) FROM stdin;
5	0	Follow up	3	0	ddeab6db-78d3-47a2-8ac1-f7d384e77fba
2	2	LMB-2	2	0	dd343921-225a-4705-8b1c-82fafa18372a
\.

COPY periods (id, name, arm_id, start_day, duration_quantity, duration_unit, repetitions, version) FROM stdin;
1	Treatment cycle	2	1	28	day	2	6
\.


COPY planned_events (id, version, activity_id, period_id, "day", details) FROM stdin;
4	0	42	1	1	\N
5	0	57	1	1	\N
6	0	58	1	8	transamines
8	0	59	1	1	\N
9	0	59	1	25	\N
10	0	26	1	8	PLT
12	0	26	1	25	PLT
13	0	58	1	15	transamines
14	0	26	1	15	PLT
15	0	56	1	1	IV over 30 minutes
16	0	56	1	3	IV over 30 minutes
17	0	56	1	5	IV over 30 minutes
\.


INSERT INTO csm_protection_group
		(
		protection_group_name, protection_group_description, 
		application_id, 
		large_element_count_flag, update_date, 
		parent_protection_group_id
		) 
	VALUES (
		'Warren Grant Magnuson Clinical Center - NCI', '', 
		(SELECT application_id FROM csm_application WHERE application_name='study_calendar'),
		0, '2007-01-25 00:00:00',
		(SELECT protection_group_id FROM csm_protection_group WHERE protection_group_name='BaseSitePG')
		);

DELETE FROM sites;
COPY sites (id, name, version, big_id) FROM stdin;
1	default	0	\N
2	Warren Grant Magnuson Clinical Center - NCI	0	NCI
\.


DELETE FROM study_sites;
COPY study_sites (id, site_id, study_id, version) FROM stdin;
1	2	1	0
\.


COMMIT