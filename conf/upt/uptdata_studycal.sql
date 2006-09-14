INSERT INTO public.csm_application
  (application_id,application_name,application_description,declarative_flag,active_flag,update_date)
VALUES(1,'csm_upt','UPT Super Admin Application',0,0,null);

INSERT INTO public.csm_application
  (application_id,application_name,application_description,declarative_flag,active_flag,update_date)
VALUES(2,'study_calendar','study calendar',null,1,'8/29/2006');

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

COMMIT;
INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(1,'StudyAdmin_Template','group having access rights to approve template',2,0,'9/7/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(6,'participantcrdn_createParticipant','group having access to create and assign participant',2,0,'9/7/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(7,'researchassoc_ViewCalendar','group with access to view calendar',2,0,'9/7/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(3,'studycrdn_NewStudy','group having access to create new study, epochs, arms, periods and activities',2,0,'9/7/2006',null);

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

COMMIT;
INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(1,7,null,2,1,'8/30/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(2,8,null,2,1,'9/7/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(3,3,null,3,3,'9/7/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(4,4,null,3,3,'9/7/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(5,5,null,5,7,'9/7/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(6,6,null,5,7,'9/7/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(7,9,null,4,6,'9/7/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id,user_id,group_id,role_id,protection_group_id,update_date)
VALUES(8,10,null,4,6,'9/7/2006');

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
VALUES(45,1,18,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(46,1,15,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(47,1,8,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(48,1,11,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(49,1,16,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(70,3,9,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(71,3,14,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(72,3,5,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(73,3,4,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(74,3,18,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(75,3,6,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(76,3,7,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(77,3,13,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(78,3,12,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(79,3,15,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(80,3,11,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(81,3,3,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(82,3,16,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(89,6,17,null);

INSERT INTO public.csm_pg_pe
  (pg_pe_id,protection_group_id,protection_element_id,update_date)
VALUES(90,6,15,null);

COMMIT;