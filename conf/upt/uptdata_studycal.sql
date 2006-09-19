INSERT INTO public.csm_application
  (application_id,application_name,application_description,declarative_flag,active_flag,update_date)
VALUES(1,'csm_upt','UPT Super Admin Application',0,0,null);

INSERT INTO public.csm_application
  (application_id,application_name,application_description,declarative_flag,active_flag,update_date)
VALUES(2,'study_calendar','study calendar',null,1,'8/29/2006');

COMMIT;
INSERT INTO public.csm_group
  (group_id,group_name,group_desc,update_date,application_id)
VALUES(1,'STUDY_COORDINATOR','study coordinator group','9/18/2006',2);

INSERT INTO public.csm_group
  (group_id,group_name,group_desc,update_date,application_id)
VALUES(2,'STUDY_ADMIN','study administrators group','9/18/2006',2);

INSERT INTO public.csm_group
  (group_id,group_name,group_desc,update_date,application_id)
VALUES(3,'PARTICIPANT_COORDINATOR','participant coordinator group','9/18/2006',2);

INSERT INTO public.csm_group
  (group_id,group_name,group_desc,update_date,application_id)
VALUES(4,'SUPER_USER','super user group','9/18/2006',2);

INSERT INTO public.csm_group
  (group_id,group_name,group_desc,update_date,application_id)
VALUES(5,'RESEARCH_ASSOCIATE','research associate group','9/18/2006',2);

COMMIT;

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(1,'CREATE','This privilege grants permission to a user to create an entity. This entity can be an object, a database entry, or a resource such as a network connection',null);

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(2,'ACCESS','This privilege allows a user to access a particular resource.  Examples of resources include a network or database connection, socket, module of the application, or even the application itself',null);

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(3,'READ','This privilege permits the user to read data from a file, URL, database, an object, etc. This can be used at an entity level signifying that the user is allowed to read data about a particular entry',null);

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(4,'WRITE','This privilege allows a user to write data to a file, URL, database, an object, etc. This can be used at an entity level signifying that the user is allowed to write data about a particular entity',null);

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(5,'UPDATE','This privilege grants permission at an entity level and signifies that the user is allowed to update data for a particular entity. Entities may include an object, object attribute, database row etc',null);

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(6,'DELETE','This privilege permits a user to delete a logical entity. This entity can be an object, a database entry, a resource such as a network connection, etc',null);

INSERT INTO public.csm_privilege
  (privilege_id,privilege_name,privilege_description,update_date)
VALUES(7,'EXECUTE','This privilege allows a user to execute a particular resource. The resource can be a method, function, behavior of the application, URL, button etc',null);

COMMIT;
INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(1,'csm_upt','UPT Super Admin Application','csm_upt',null,null,1,null);

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(2,'study_calendar',null,'study_calendar',null,null,1,'8/29/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(3,'CreateStudyLink','the jsp hyperlink to create new study','CreateStudyLink','CreateStudyLink',null,2,'8/30/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(4,'CreateArmsLink','hyperlink on jsp to create Arms Link','CreateArmsLink','CreateArmsLink',null,2,'8/30/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(5,'CreatePeriodsLink','hyperlink on jsp to access the create periods page','CreatePeriodsLink','CreatePeriodsLink',null,2,'8/30/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(6,'CreateActivitiesLink','hyperlink on jsp to create activities','CreateActivitiesLink','CreateActivitiesLink',null,2,'8/30/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(7,'AddActivitiesLink','hyperlink on jsp for add activities to a study','AddActivitiesLink','AddActivitiesLink',null,2,'8/30/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(8,'ApproveStudyCalendarTemplate','link to mark template complete','ApproveStudyCalendarTemplate','ApproveStudyCalendarTemplate',null,2,'8/30/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(9,'/studycalendar/pages/newStudy','url for new study','/studycalendar/pages/newStudy','/studycalendar/pages/newStudy',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(10,'/studycalendar/pages/createParticipant','url for create participant','/studycalendar/pages/createParticipant','/studycalendar/pages/createParticipant',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(11,'/studycalendar/pages/studyList','url for list of studies','/studycalendar/pages/studyList','/studycalendar/pages/studyList',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(12,'/studycalendar/pages/viewArm','url for view arm details','/studycalendar/pages/viewArm','/studycalendar/pages/viewArm',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(13,'/studycalendar/pages/newPeriod','url for new period','/studycalendar/pages/newPeriod','/studycalendar/pages/newPeriod',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(14,'/studycalendar/pages/newActivity','url for new activity','/studycalendar/pages/newActivity','/studycalendar/pages/newActivity',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(15,'/studycalendar/pages/welcome','welcome page','/studycalendar/pages/welcome','/studycalendar/pages/welcome',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(16,'/studycalendar/pages/calendarTemplate','calendar template url','/studycalendar/pages/calendarTemplate','/studycalendar/pages/calendarTemplate',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(17,'/studycalendar/pages/assignParticipant','assign participant url','/studycalendar/pages/assignParticipant','/studycalendar/pages/assignParticipant',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(18,'/studycalendar/public/login','login page url','/studycalendar/public/login','/studycalendar/public/login',null,2,'9/7/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(19,'CreateParticipantLink','link for create participant page','CreateParticipantLink','CreateParticipantLink',null,2,'9/14/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(20,'AssignParticipantLink','link to assign participant to a study','AssignParticipantLink','AssignParticipantLink',null,2,'9/14/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(21,'CreateEpochLink','link to create epoch','CreateEpochLink','CreateEpochLink',null,2,'9/14/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(22,'/studycalendar/pages/markComplete','url for mark template complete','/studycalendar/pages/markComplete','/studycalendar/pages/markComplete',null,2,'9/14/2006');

INSERT INTO public.csm_protection_element
  (protection_element_id,protection_element_name,protection_element_description,object_id,attribute,protection_element_type_id,application_id,update_date)
VALUES(23,'/studycalendar/pages/managePeriod','url for manage period','/studycalendar/pages/managePeriod','/studycalendar/pages/managePeriod',null,2,'9/19/2006');

COMMIT;
INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(9,'BaseAccess','group of features which all users have access to',2,0,'9/14/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(10,'CreateStudyAccess','access to create study, epochs, arms and associate periods to arms',2,0,'9/14/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(11,'MarkTemplatCompleteAccess','access to mark template complete',2,0,'9/14/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(12,'ParticipantAssignmentAccess','access to create and assign participants',2,0,'9/14/2006',null);

COMMIT;
INSERT INTO public.csm_role
  (role_id,role_name,role_description,application_id,active_flag,update_date)
VALUES(2,'STUDY_ADMIN','study administrator',2,1,'8/30/2006');

INSERT INTO public.csm_role
  (role_id,role_name,role_description,application_id,active_flag,update_date)
VALUES(3,'STUDY_COORDINATOR','study coordinator',2,1,'9/7/2006');

INSERT INTO public.csm_role
  (role_id,role_name,role_description,application_id,active_flag,update_date)
VALUES(4,'PARTICIPANT_COORDINATOR','participant coordinator',2,1,'9/7/2006');

INSERT INTO public.csm_role
  (role_id,role_name,role_description,application_id,active_flag,update_date)
VALUES(5,'RESEARCH_ASSOCIATE','clinical research associate',2,1,'9/7/2006');

INSERT INTO public.csm_role
  (role_id,role_name,role_description,application_id,active_flag,update_date)
VALUES(6,'SUPERUSER','super user for this application for testing purposes',2,1,'9/14/2006');

COMMIT;
INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(7,2,2,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(9,3,2,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(10,4,2,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(11,5,2,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(12,6,7,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(13,6,6,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(14,6,5,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(15,6,3,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(16,6,2,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(17,6,4,null);

INSERT INTO public.csm_role_privilege
  (role_privilege_id,role_id,privilege_id,update_date)
VALUES(18,6,1,null);

COMMIT;
INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(1,'SYSTEM_ADMIN','SYSTEM_ADMIN','SYSTEM_ADMIN',null,null,null,null,'system_admin',null,null,null,null);

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(5,'ra_1','Research','Associate1','','','','','ra_1','',null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(6,'ra_2','Research','Associate2','','','','','ra_2','',null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(7,'study_admin1','study','admin1','','','','','study_admin1','',null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(8,'study_admin2','Study','Admin2','','','','','study_admin2','',null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(3,'studycd_1','Clinical_Research','StudyCoordinator1','','','','','studycd_1','',null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(4,'studycd_2','Clinical Research','StudyCoordinator2','','','','','studycd_2','',null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(9,'participantcd_1','participant','coordinator','','','','','participantcd_1','',null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(10,'participantcd_2','Participant','Coordinator2','','','','','participantcd_2','',null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(2,'sc_systemadmin','STUDYCAL_SYSTEMADMIN','STUDYCAL_SYSTEMADMIN','','','','','systemadmin','',null,null,'9/12/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(11,'superuser','super','superuser','','','','','superuser','',null,null,'9/14/2006');

COMMIT;
INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(1,3,1);

INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(2,4,1);

INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(3,7,2);

INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(4,8,2);

INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(5,5,5);

INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(6,6,5);

INSERT INTO public.csm_user_group
  (user_group_id,user_id,group_id)
VALUES(7,11,4);

COMMIT;
INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(21,9,null,4,12,'9/15/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(22,9,null,4,9,'9/15/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(23,10,null,4,9,'9/15/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(24,10,null,4,12,'9/15/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(29,null,1,3,9,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(30,null,1,3,10,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(31,null,2,2,9,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(32,null,2,2,11,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(33,null,3,4,9,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(34,null,3,4,12,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(35,null,4,6,9,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(36,null,4,6,10,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(37,null,4,6,11,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(38,null,4,6,12,'9/18/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(39,null,5,5,9,'9/18/2006');

COMMIT;
INSERT INTO public.csm_user_pe
  (user_protection_element_id,protection_element_id,user_id,update_date)
VALUES(1,1,1,null);

INSERT INTO public.csm_user_pe
  (user_protection_element_id,protection_element_id,user_id,update_date)
VALUES(2,2,2,null);

COMMIT;

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(123,9,18,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(124,9,16,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(125,9,15,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(126,9,11,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(137,11,8,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(138,11,22,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(139,12,19,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(140,12,20,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(141,12,10,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(142,12,17,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(143,10,7,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(144,10,6,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(145,10,12,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(146,10,5,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(147,10,21,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(148,10,4,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(149,10,9,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(150,10,14,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(151,10,23,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(152,10,3,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(153,10,13,null);

COMMIT;

select setval('csm_applicati_application__seq', 2);
select setval('csm_privilege_privilege_id_seq', 7);
select setval('csm_protectio_protection_e_seq', 23);
select setval('csm_protectio_protection_g_seq', 12);
select setval('csm_role_role_id_seq', 6);
select setval('csm_role_priv_seq', 18);
select setval('csm_user_user_id_seq', 11);
select setval('csm_user_grou_user_group_r_seq', 39);
select setval('csm_user_pe_user_protectio_seq', 2);
select setval('csm_pg_pe_id_seq', 153);
select setval('csm_group_group_id_seq', 5);
select setval('csm_user_group_id_seq', 7);