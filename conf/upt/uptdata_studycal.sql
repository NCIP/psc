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


INSERT INTO public.csm_group
  (group_id, group_name, group_desc, update_date, application_id)
VALUES(6, 'SITE_COORDINATOR', 'site coordinator group', '10/31/2006', 2);

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

COMMIT;
INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(9,'BaseAccess','group of features which all users have access to',2,0,'9/14/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(10,'CreateStudyAccess','access to create study, epochs, arms and associate periods to arms',2,0,'9/14/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id, protection_group_name, protection_group_description, application_id, large_element_count_flag, update_date, parent_protection_group_id)
VALUES(11, 'AdministrativeAccess', 'access to mark template complete', 2, 0, '10/31/2006', null);

INSERT INTO public.csm_protection_group
  (protection_group_id,protection_group_name,protection_group_description,application_id,large_element_count_flag,update_date,parent_protection_group_id)
VALUES(12,'ParticipantAssignmentAccess','access to create and assign participants',2,0,'9/14/2006',null);

INSERT INTO public.csm_protection_group
  (protection_group_id, protection_group_name, protection_group_description, application_id, large_element_count_flag, update_date, parent_protection_group_id)
VALUES(13, 'SiteCoordinatorAccess', 'access for site coordinators', 2, 0, '10/31/2006', null);

INSERT INTO public.csm_protection_group
  (protection_group_id, protection_group_name, protection_group_description, application_id, large_element_count_flag, update_date, parent_protection_group_id)
VALUES(15, 'BaseSitePG', 'Base site protection group', 2, 0, '10/31/2006', null);

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


INSERT INTO public.csm_role
  (role_id, role_name, role_description, application_id, active_flag, update_date)
VALUES(7, 'SITE_COORDINATOR', 'site coordinator role', 2, 1, '10/31/2006');

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

INSERT INTO public.csm_role_privilege
  (role_privilege_id, role_id, privilege_id, update_date)
VALUES(19, 7, 2, null);

COMMIT;
INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(1,'SYSTEM_ADMIN','SYSTEM_ADMIN','SYSTEM_ADMIN',null,null,null,null,'system_admin',null,null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(5,'ra_1','Research','Associate1',null,null,null,null,'ra_1',null,null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(6,'ra_2','Research','Associate2',null,null,null,null,'ra_2',null,null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(7,'study_admin1','study','admin1',null,null,null,null,'study_admin1',null,null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(8,'study_admin2','Study','Admin2',null,null,null,null,'study_admin2',null,null,null,'8/30/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(3,'studycd_1','Clinical_Research','StudyCoordinator1',null,null,null,null,'studycd_1',null,null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(4,'studycd_2','Clinical Research','StudyCoordinator2',null,null,null,null,'studycd_2',null,null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(9,'participantcd_1','participant','coordinator',null,null,null,null,'participantcd_1',null,null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(10,'participantcd_2','Participant','Coordinator2',null,null,null,null,'participantcd_2',null,null,null,'9/7/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(2,'sc_systemadmin','STUDYCAL_SYSTEMADMIN','STUDYCAL_SYSTEMADMIN',null,null,null,null,'systemadmin',null,null,null,'9/12/2006');

INSERT INTO public.csm_user
  (user_id,login_name,first_name,last_name,organization,department,title,phone_number,password,email_id,start_date,end_date,update_date)
VALUES(11,'superuser','super','superuser',null,null,null,null,'superuser',null,null,null,'9/14/2006');

INSERT INTO public.csm_user
  (user_id, login_name, first_name, last_name, organization, department, title, phone_number, password, email_id, start_date, end_date, update_date)
VALUES(12, 'sitecd_1', 'sitecd_1', 'coordinator1', null, null, null, null, 'sitecd_1', null, null, null, '10/31/2006');

INSERT INTO public.csm_user
  (user_id, login_name, first_name, last_name, organization, department, title, phone_number, password, email_id, start_date, end_date, update_date)
VALUES(13, 'sitecd_2', 'sitecd_2', 'coordinator2', null, null, null, null, 'sitecd_2', null, null, null, '10/31/2006');

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

INSERT INTO public.csm_user_group
  (user_group_id, user_id, group_id)
VALUES(8, 13, 6);

INSERT INTO public.csm_user_group
  (user_group_id, user_id, group_id)
VALUES(10, 9, 3);

INSERT INTO public.csm_user_group
  (user_group_id, user_id, group_id)
VALUES(11, 10, 3);

INSERT INTO public.csm_user_group
  (user_group_id, user_id, group_id)
VALUES(13, 12, 6);

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

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id, user_id, group_id, role_id, protection_group_id, update_date)
VALUES (40, null, 6, 7, 9, '10/31/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id, user_id, group_id, role_id, protection_group_id, update_date)
VALUES (41, null, 6, 7, 13, '10/31/2006');

INSERT INTO public.csm_user_group_role_pg
  (user_group_role_pg_id, user_id, group_id, role_id, protection_group_id, update_date)
VALUES(42, NULL, 4, 7, 13, '10/31/2006');

COMMIT;
INSERT INTO public.csm_user_pe
  (user_protection_element_id,protection_element_id,user_id,update_date)
VALUES(1,1,1,null);

INSERT INTO public.csm_user_pe
  (user_protection_element_id,protection_element_id,user_id,update_date)
VALUES(2,2,2,null);

COMMIT;


select setval('csm_applicati_application__seq', 2);
select setval('csm_privilege_privilege_id_seq', 7);
select setval('csm_protectio_protection_e_seq', 2);
select setval('csm_protectio_protection_g_seq', 15);
select setval('csm_role_role_id_seq', 7);
select setval('csm_role_priv_seq', 19);
select setval('csm_user_user_id_seq', 13);
select setval('csm_user_grou_user_group_r_seq', 42);
select setval('csm_user_pe_user_protectio_seq', 2);
select setval('csm_group_group_id_seq', 6);
select setval('csm_user_group_id_seq', 13);