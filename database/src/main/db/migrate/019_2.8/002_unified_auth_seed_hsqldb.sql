INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 1,	'system_administrator',	'Configures the technical system level properties and behavior of the applications (i.e. the password policy, email setup, ESB, etc).', (select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 2,	'business_administrator',	'Manages the domain related application wide properties and behavior (i.e. label names, reference data lists, etc)',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 3,	'person_and_organization_information_manager',	'Manages organizations and rosters / Creates and updates person info including contact info, degrees/certifications, rosters they?re associated with',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 4,	'data_importer',	'Identifies studies defined by Coordinating Center and imports as a consumer that data defined elsewhere',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 5,	'user_administrator',	'Ability to read system personnel (research staff and investigators) and create/manage user accounts/application roles, defines Custom Combination Roles', 		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 6,	'study_qa_manager',	'Updates core study info after saving / opening study.(e.g. PI, title, phase, epochs/arms, basic study design)  Read-only review of study calendar template / releases templates to participating sites', 		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 7,	'study_creator',	'Creates the core study info (e.g. PI, title, description, phase, epochs/arms & basic study design, etc.) NOTE:  some sites may want to combine the supplemental study info roles into this role', 		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 8,	'supplemental_study_information_manager',	'Adds treatment assignment, drugs, adEERS-specific diseases, adEERs reporting criteria, CTC/MedDRA version, etc.  Update/manage registration metadata (e.g. stratifications, eligibility criteria, notifi',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES ( 9,	'study_team_administrator',	'Connects study level people to study and internal staff to study. Assigns internal staff to protocol, determines study artifact accessiblity for staff (e.g. study calendar templates, CRFs, etc.)',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (10,	'study_site_participation_administrator',	'Connects participating sites to a protocol',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (11, 'ae_rule_and_report_manager',	'Creates, manages, imports AE rules / Creates, manages, imports AE report definitions',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (12,	'study_calendar_template_builder',	'Creates and updates study calendar templates', 		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (13,	'registration_qa_manager',	'Updates registration information (study subject ID, Date of consent) after enrollment. Can waive the eligibility criteria for certain study subjects.', 		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (14, 'subject_manager',	'Defines patient to system (remaining subject data managed by other roles which are not defined)',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (15,	'study_subject_calendar_manager',	'Creates and updates a subject-specific study calendar based on a study calendar template', 		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (16,	'registrar',	'Accepts and approves/denies subject registration requests.  Requests subject registration on a particular study',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (17,	'ae_reporter',	'Creates / updates info about AE that needs reported / submits report to appropriate parties per report definition. Enters set of required AEs to be assessed and any other AEs that patient experienced.',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (18,	'ae_expedited_report_reviewer',	'Read-only: reviews, provides comments, and routes expedited reports through the review workflow',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (19,	'ae_study_data_reviewer',	'Read-only: reviews, provides comments, and adverse event data through a review workflow',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (20,	'lab_impact_calendar_notifier',	'Creates a calendar notification for a potential lab-based treatment modification',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (21,	'lab_data_user',	'Enters, edits, and imports labs from LIMS, viewing labs, selecting and sending labs to CDMS and caAERS',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (22,	'data_reader',	'Read-only: typically not part of org being audited, granted temporary access (no updates) to whole study or specific study/subjects, or any data entered by site for study/subject, crosses all apps.',		(select application_id from csm_application where application_name = 'study_calendar'));

INSERT INTO csm_group(group_id, group_name, group_desc, application_id) VALUES (23, 'data_analyst',	'Read-only: searches for data, uses built-in analysis tools, exports data to third party tools',		(select application_id from csm_application where application_name = 'study_calendar'));


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 1,	'system_administrator',	'Configures the technical system level properties and behavior of the applications (i.e. the password policy, email setup, ESB, etc).',	(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 1,	'system_administrator',	'Configures the technical system level properties and behavior of the applications (i.e. the password policy, email setup, ESB, etc).');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 1, 1, 1);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 2,	'business_administrator',	'Manages the domain related application wide properties and behavior (i.e. label names, reference data lists, etc)',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 2,	'business_administrator',	'Manages the domain related application wide properties and behavior (i.e. label names, reference data lists, etc)');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 2, 2, 2);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 3,	'person_and_organization_information_manager',	'Manages organizations and rosters / Creates and updates person info including contact info, degrees/certifications, rosters they?re associated with',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 3,	'person_and_organization_information_manager',	'Manages organizations and rosters / Creates and updates person info including contact info, degrees/certifications, rosters they?re associated with');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 3, 3, 3);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 4,	'data_importer',	'Identifies studies defined by Coordinating Center and imports as a consumer that data defined elsewhere',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 4,	'data_importer',	'Identifies studies defined by Coordinating Center and imports as a consumer that data defined elsewhere');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 4, 4, 4);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 5,	'user_administrator',	'Ability to read system personnel (research staff and investigators) and create/manage user accounts/application roles, defines Custom Combination Roles', 		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 5,	'user_administrator',	'Ability to read system personnel (research staff and investigators) and create/manage user accounts/application roles, defines Custom Combination Roles');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 5, 5, 5);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 6,	'study_qa_manager',	'Updates core study info after saving / opening study.(e.g. PI, title, phase, epochs/arms, basic study design)  Read-only review of study calendar template / releases templates to participating sites', 		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 6,	'study_qa_manager',	'Updates core study info after saving / opening study.(e.g. PI, title, phase, epochs/arms, basic study design)  Read-only review of study calendar template / releases templates to participating sites');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 6, 6, 6);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 7,	'study_creator',	'Creates the core study info (e.g. PI, title, description, phase, epochs/arms & basic study design, etc.) NOTE:  some sites may want to combine the supplemental study info roles into this role', 		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 7,	'study_creator',	'Creates the core study info (e.g. PI, title, description, phase, epochs/arms & basic study design, etc.) NOTE:  some sites may want to combine the supplemental study info roles into this role');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 7, 7, 7);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 8,	'supplemental_study_information_manager',	'Adds treatment assignment, drugs, adEERS-specific diseases, adEERs reporting criteria, CTC/MedDRA version, etc.  Update/manage registration metadata (e.g. stratifications, eligibility criteria, notifi',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 8,	'supplemental_study_information_manager',	'Adds treatment assignment, drugs, adEERS-specific diseases, adEERs reporting criteria, CTC/MedDRA version, etc.  Update/manage registration metadata (e.g. stratifications, eligibility criteria, notifi');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 8, 8, 8);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES ( 9,	'study_team_administrator',	'Connects study level people to study and internal staff to study. Assigns internal staff to protocol, determines study artifact accessiblity for staff (e.g. study calendar templates, CRFs, etc.)',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES ( 9,	'study_team_administrator',	'Connects study level people to study and internal staff to study. Assigns internal staff to protocol, determines study artifact accessiblity for staff (e.g. study calendar templates, CRFs, etc.)');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES ( 9, 9, 9);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (10,	'study_site_participation_administrator',	'Connects participating sites to a protocol',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (10,	'study_site_participation_administrator',	'Connects participating sites to a protocol');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (10,10,10);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (11,  'ae_rule_and_report_manager',	'Creates, manages, imports AE rules / Creates, manages, imports AE report definitions',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (11,  'ae_rule_and_report_manager',	'Creates, manages, imports AE rules / Creates, manages, imports AE report definitions');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (11,11,11);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (12,	'study_calendar_template_builder',	'Creates and updates study calendar templates', 		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (12,	'study_calendar_template_builder',	'Creates and updates study calendar templates');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (12,12,12);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (13,	'registration_qa_manager',	'Updates registration information (study subject ID, Date of consent) after enrollment. Can waive the eligibility criteria for certain study subjects.', 		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (13,	'registration_qa_manager',	'Updates registration information (study subject ID, Date of consent) after enrollment. Can waive the eligibility criteria for certain study subjects.');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (13,13,13);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (14, 'subject_manager',	'Defines patient to system (remaining subject data managed by other roles which are not defined)',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (14, 'subject_manager',	'Defines patient to system (remaining subject data managed by other roles which are not defined)');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (14,14,14);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (15,	'study_subject_calendar_manager',	'Creates and updates a subject-specific study calendar based on a study calendar template', 		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (15,	'study_subject_calendar_manager',	'Creates and updates a subject-specific study calendar based on a study calendar template');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (15,15,15);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (16,	'registrar',	'Accepts and approves/denies subject registration requests. Requests subject registration on a particular study',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (16,	'registrar',	'Accepts and approves/denies subject registration requests. Requests subject registration on a particular study');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (16,16,16);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (17,	'ae_reporter',	'Creates / updates info about AE that needs reported / submits report to appropriate parties per report definition. Enters set of required AEs to be assessed and any other AEs that patient experienced.',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (17,	'ae_reporter',	'Creates / updates info about AE that needs reported / submits report to appropriate parties per report definition. Enters set of required AEs to be assessed and any other AEs that patient experienced.');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (17,17,17);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (18,	'ae_expedited_report_reviewer',	'Read-only: reviews, provides comments, and routes expedited reports through the review workflow',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (18,	'ae_expedited_report_reviewer',	'Read-only: reviews, provides comments, and routes expedited reports through the review workflow');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (18,18,18);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (19,	'ae_study_data_reviewer',	'Read-only: reviews, provides comments, and adverse event data through a review workflow',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (19,	'ae_study_data_reviewer',	'Read-only: reviews, provides comments, and adverse event data through a review workflow');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (19,19,19);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (20,	'lab_impact_calendar_notifier',	'Creates a calendar notification for a potential lab-based treatment modification',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (20,	'lab_impact_calendar_notifier',	'Creates a calendar notification for a potential lab-based treatment modification');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (20,20,20);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (21,	'lab_data_user',	'Enters, edits, and imports labs from LIMS, viewing labs, selecting and sending labs to CDMS and caAERS',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (21,	'lab_data_user',	'Enters, edits, and imports labs from LIMS, viewing labs, selecting and sending labs to CDMS and caAERS');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (21,21,21);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (22,	'data_reader',	'Read-only: typically not part of org being audited, granted temporary access (no updates) to whole study or specific study/subjects, or any data entered by site for study/subject, crosses all apps.',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (22,	'data_reader',	'Read-only: typically not part of org being audited, granted temporary access (no updates) to whole study or specific study/subjects, or any data entered by site for study/subject, crosses all apps.');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (22,22,22);


INSERT INTO csm_role(role_id, role_name, role_description, application_id, active_flag) VALUES (23, 'data_analyst',	'Read-only: searches for data, uses built-in analysis tools, exports data to third party tools',		(select application_id from csm_application where application_name = 'study_calendar'), 1);

INSERT INTO csm_privilege(privilege_id, privilege_name, privilege_description) VALUES (23, 'data_analyst',	'Read-only: searches for data, uses built-in analysis tools, exports data to third party tools');

INSERT INTO csm_role_privilege(role_privilege_id, role_id, privilege_id) VALUES (23,23,23);
