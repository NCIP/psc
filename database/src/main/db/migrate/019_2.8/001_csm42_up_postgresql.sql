-- The refresh script which upgrades PSC's internal CSM schema to CSM 4.2.
-- For simplicity, it drops all tables except csm_application and csm_user 
-- since their old uses can't be translated to the new unified security model
-- in any case.

-- The csm_user and csm_application tables are updated in the associated
-- migration.

-- It also ignores the one trigger that CSM included because it didn't seem
-- useful and complicated deployment.

DROP TABLE CSM_PG_PE CASCADE;

DROP TABLE CSM_USER_GROUP CASCADE;

DROP TABLE CSM_ROLE_PRIVILEGE CASCADE;

DROP TABLE CSM_USER_GROUP_ROLE_PG CASCADE;

DROP TABLE CSM_USER_PE CASCADE;

DROP TABLE CSM_PROTECTION_GROUP CASCADE;

DROP TABLE CSM_PROTECTION_ELEMENT CASCADE;

DROP TABLE CSM_PRIVILEGE CASCADE;

DROP TABLE CSM_ROLE CASCADE;

DROP TABLE CSM_GROUP CASCADE;

DROP SEQUENCE CSM_GROUP_GROUP_ID_SEQ;

DROP SEQUENCE CSM_PG_PE_ID_SEQ;

DROP SEQUENCE CSM_PRIVILEGE_PRIVILEGE_ID_SEQ;

DROP SEQUENCE CSM_PROTECTIO_PROTECTION_E_SEQ;

DROP SEQUENCE CSM_PROTECTIO_PROTECTION_G_SEQ;

DROP SEQUENCE CSM_ROLE_ROLE_ID_SEQ;

DROP SEQUENCE CSM_ROLE_PRIV_SEQ;

DROP SEQUENCE CSM_USER_GROUP_ID_SEQ;

DROP SEQUENCE CSM_USER_GROU_USER_GROUP_R_SEQ;

DROP SEQUENCE CSM_USER_PE_USER_PROTECTIO_SEQ;

CREATE SEQUENCE CSM_FILTER_CLAUSE_FILTE_ID_SEQ;

CREATE TABLE CSM_FILTER_CLAUSE ( 
FILTER_CLAUSE_ID int8 not null default nextval('CSM_FILTER_CLAUSE_FILTE_ID_SEQ'),
CLASS_NAME character varying(100) NOT NULL,
FILTER_CHAIN character varying(2000) NOT NULL,
TARGET_CLASS_NAME character varying(100) NOT NULL,
TARGET_CLASS_ATTRIBUTE_NAME character varying(100) NOT NULL,
TARGET_CLASS_ATTRIBUTE_TYPE character varying(100) NOT NULL,
TARGET_CLASS_ALIAS character varying(100),
TARGET_CLASS_ATTRIBUTE_ALIAS character varying(100),
GENERATED_SQL_USER character varying(4000) NOT NULL,
GENERATED_SQL_GROUP character varying(4000) NOT NULL,
APPLICATION_ID int8 NOT NULL,
UPDATE_DATE DATE NOT NULL default current_date,
PRIMARY KEY(FILTER_CLAUSE_ID)
);

CREATE SEQUENCE CSM_MAPPING_MAPPING_SEQ;

CREATE TABLE CSM_MAPPING (
MAPPING_ID int8 NOT NULL default nextval('CSM_MAPPING_MAPPING_SEQ'),
APPLICATION_ID int8 NOT NULL,
OBJECT_NAME character varying(100) NOT NULL,
ATTRIBUTE_NAME character varying(100) NOT NULL,
OBJECT_PACKAGE_NAME character varying(100),
TABLE_NAME character varying(100),
TABLE_NAME_GROUP character varying(100),
TABLE_NAME_USER character varying(100),
VIEW_NAME_GROUP character varying(100),
VIEW_NAME_USER character varying(100),
ACTIVE_FLAG smallint NOT NULL default 0,
MAINTAINED_FLAG smallint NOT NULL default 0,
UPDATE_DATE  DATE NOT NULL default current_date,
PRIMARY KEY(MAPPING_ID)
);

CREATE SEQUENCE CSM_GROUP_GROUP_ID_SEQ;

CREATE TABLE CSM_GROUP
(
GROUP_ID int8 default nextval('CSM_GROUP_GROUP_ID_SEQ'), 
GROUP_NAME character varying(255) NOT NULL,
GROUP_DESC character varying(200),
UPDATE_DATE date NOT NULL default current_date,
APPLICATION_ID int8 NOT NULL,
PRIMARY KEY (GROUP_ID)
) ;

CREATE SEQUENCE CSM_PRIVILEGE_PRIVILEGE_ID_SEQ ;

CREATE TABLE CSM_PRIVILEGE ( 
PRIVILEGE_ID int8 default nextval('CSM_PRIVILEGE_PRIVILEGE_ID_SEQ'), 
PRIVILEGE_NAME character varying(100) NOT NULL,
PRIVILEGE_DESCRIPTION character varying(200),
UPDATE_DATE date NOT NULL default current_date,
PRIMARY KEY(PRIVILEGE_ID)
);

CREATE SEQUENCE CSM_PROTECTIO_PROTECTION_E_SEQ;

CREATE TABLE CSM_PROTECTION_ELEMENT (
PROTECTION_ELEMENT_ID int8 default nextval('CSM_PROTECTIO_PROTECTION_E_SEQ'), 
PROTECTION_ELEMENT_NAME character varying(100) NOT NULL,
PROTECTION_ELEMENT_DESCRIPTION character varying(200),
OBJECT_ID character varying(100) NOT NULL,
ATTRIBUTE character varying(100) ,
ATTRIBUTE_VALUE character varying(100) ,
PROTECTION_ELEMENT_TYPE character varying(100),
APPLICATION_ID int8 NOT NULL,
UPDATE_DATE date NOT NULL default current_date,
PRIMARY KEY(PROTECTION_ELEMENT_ID)
);

CREATE SEQUENCE CSM_PROTECTIO_PROTECTION_G_SEQ;

CREATE TABLE CSM_PROTECTION_GROUP ( 
PROTECTION_GROUP_ID int8 default nextval('CSM_PROTECTIO_PROTECTION_G_SEQ'), 
PROTECTION_GROUP_NAME character varying(100) NOT NULL,
PROTECTION_GROUP_DESCRIPTION character varying(200),
APPLICATION_ID int8 NOT NULL,
LARGE_ELEMENT_COUNT_FLAG smallint NOT NULL,
UPDATE_DATE date NOT NULL default current_date,
PARENT_PROTECTION_GROUP_ID int8,
PRIMARY KEY(PROTECTION_GROUP_ID)
);


CREATE SEQUENCE CSM_PG_PE_PG_PE_ID_SEQ;

CREATE TABLE CSM_PG_PE ( 
PG_PE_ID int8 default nextval('CSM_PG_PE_PG_PE_ID_SEQ'), 
PROTECTION_GROUP_ID int8 NOT NULL,
PROTECTION_ELEMENT_ID int8 NOT NULL,
UPDATE_DATE date default current_date,
PRIMARY KEY(PG_PE_ID)
);

CREATE SEQUENCE CSM_ROLE_ROLE_ID_SEQ;

CREATE TABLE CSM_ROLE ( 
ROLE_ID int8 default nextval('CSM_ROLE_ROLE_ID_SEQ'), 
ROLE_NAME character varying(100) NOT NULL,
ROLE_DESCRIPTION character varying(200),
APPLICATION_ID int8 NOT NULL,
ACTIVE_FLAG smallint NOT NULL,
UPDATE_DATE date NOT NULL default current_date,
PRIMARY KEY(ROLE_ID)
);

CREATE SEQUENCE CSM_ROLE_PRIV_ROLE_PRIVILE_SEQ;

CREATE TABLE CSM_ROLE_PRIVILEGE ( 
ROLE_PRIVILEGE_ID int8 default nextval('CSM_ROLE_PRIV_ROLE_PRIVILE_SEQ'), 
ROLE_ID int8 NOT NULL,
PRIVILEGE_ID int8 NOT NULL,
PRIMARY KEY(ROLE_PRIVILEGE_ID)
);

CREATE SEQUENCE CSM_USER_GROU_USER_GROUP_I_SEQ;

CREATE TABLE CSM_USER_GROUP ( 
USER_GROUP_ID int8 default nextval('CSM_USER_GROU_USER_GROUP_I_SEQ'), 
USER_ID int8 NOT NULL,
GROUP_ID int8 NOT NULL,
PRIMARY KEY(USER_GROUP_ID)
);

CREATE SEQUENCE CSM_USER_GROU_USER_GROUP_R_SEQ;

CREATE TABLE CSM_USER_GROUP_ROLE_PG ( 
USER_GROUP_ROLE_PG_ID int8 default nextval('CSM_USER_GROU_USER_GROUP_R_SEQ'), 
USER_ID int8,
GROUP_ID int8,
ROLE_ID int8 NOT NULL,
PROTECTION_GROUP_ID int8 NOT NULL,
UPDATE_DATE date NOT NULL default current_date,
PRIMARY KEY(USER_GROUP_ROLE_PG_ID)
);

CREATE SEQUENCE CSM_USER_PE_USER_PROTECTIO_SEQ;

CREATE TABLE CSM_USER_PE ( 
USER_PROTECTION_ELEMENT_ID int8 default nextval('CSM_USER_PE_USER_PROTECTIO_SEQ'), 
PROTECTION_ELEMENT_ID int8 NOT NULL,
USER_ID int8 NOT NULL,
PRIMARY KEY(USER_PROTECTION_ELEMENT_ID)
);

CREATE INDEX idx_APPLICATION_ID ON CSM_GROUP USING btree (APPLICATION_ID);

ALTER TABLE CSM_GROUP
ADD CONSTRAINT UQ_GROUP_GROUP_NAME UNIQUE (APPLICATION_ID, GROUP_NAME);

ALTER TABLE CSM_PRIVILEGE
ADD CONSTRAINT UQ_PRIVILEGE_NAME UNIQUE (PRIVILEGE_NAME);

CREATE INDEX idx_APPLICATION_ID_PE ON CSM_PROTECTION_ELEMENT USING btree (APPLICATION_ID);

CREATE INDEX idx_OBJ_ATTR_APP ON CSM_PROTECTION_ELEMENT(OBJECT_ID, ATTRIBUTE, APPLICATION_ID);

ALTER TABLE CSM_PROTECTION_ELEMENT
ADD CONSTRAINT UQ_PE_PE_NAME_ATTRIBUTE_VALUE_APP_ID UNIQUE (OBJECT_ID, ATTRIBUTE, ATTRIBUTE_VALUE, APPLICATION_ID);

CREATE INDEX idx_APPLICATION_ID_PG ON CSM_PROTECTION_GROUP USING btree (APPLICATION_ID);

ALTER TABLE CSM_PROTECTION_GROUP
ADD CONSTRAINT UQ_PROTECTION_GROUP_PROTECTION_GROUP_NAME UNIQUE (APPLICATION_ID, PROTECTION_GROUP_NAME);

CREATE INDEX idx_PARENT_PROTECTION_GROUP_ID ON CSM_PROTECTION_GROUP USING btree (PARENT_PROTECTION_GROUP_ID);

CREATE INDEX idx_PROTECTION_ELEMENT_ID ON CSM_PG_PE USING btree (PROTECTION_ELEMENT_ID);

CREATE INDEX idx_PROTECTION_GROUP_ID_PGPE ON CSM_PG_PE USING btree (PROTECTION_GROUP_ID);

ALTER TABLE CSM_PG_PE
ADD CONSTRAINT UQ_PROTECTION_GROUP_PROTECTION_ELEMENT_PROTECTION_GROUP_ID UNIQUE (PROTECTION_ELEMENT_ID, PROTECTION_GROUP_ID);

CREATE INDEX idx_APPLICATION_ID_R ON CSM_ROLE USING btree (APPLICATION_ID);

ALTER TABLE CSM_ROLE
ADD CONSTRAINT UQ_ROLE_ROLE_NAME UNIQUE (APPLICATION_ID, ROLE_NAME);

CREATE INDEX idx_PRIVILEGE_ID ON CSM_ROLE_PRIVILEGE USING btree (PRIVILEGE_ID);

ALTER TABLE CSM_ROLE_PRIVILEGE
ADD CONSTRAINT UQ_ROLE_PRIVILEGE_ROLE_ID UNIQUE (PRIVILEGE_ID, ROLE_ID);

CREATE INDEX idx_ROLE_ID ON CSM_ROLE_PRIVILEGE USING btree (ROLE_ID);

CREATE INDEX idx_USER_ID ON CSM_USER_GROUP USING btree (USER_ID);

CREATE INDEX idx_GROUP_ID ON CSM_USER_GROUP USING btree (GROUP_ID);

CREATE INDEX idx_GROUP_ID_UGRPG ON CSM_USER_GROUP_ROLE_PG USING btree (GROUP_ID);

CREATE INDEX idx_ROLE_ID_UGRPG ON CSM_USER_GROUP_ROLE_PG USING btree (ROLE_ID);

CREATE INDEX idx_PROTECTION_GROUP_ID_UGRPG ON CSM_USER_GROUP_ROLE_PG USING btree (PROTECTION_GROUP_ID);

CREATE INDEX idx_USER_ID_UGRPG ON CSM_USER_GROUP_ROLE_PG USING btree (USER_ID);

CREATE INDEX idx_USER_ID_UPE ON CSM_USER_PE USING btree (USER_ID);

CREATE INDEX idx_PROTECTION_ELEMENT_ID_UPE ON CSM_USER_PE USING btree (PROTECTION_ELEMENT_ID);

ALTER TABLE CSM_USER_PE
ADD CONSTRAINT UQ_USER_PROTECTION_ELEMENT_PROTECTION_ELEMENT_ID UNIQUE (USER_ID, PROTECTION_ELEMENT_ID);

ALTER TABLE CSM_GROUP ADD CONSTRAINT FK_APPLICATION_GROUP 
FOREIGN KEY (APPLICATION_ID) REFERENCES CSM_APPLICATION (APPLICATION_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_FILTER_CLAUSE ADD CONSTRAINT FK_APPLICATION_FILTER_CLAUSE 
FOREIGN KEY (APPLICATION_ID) REFERENCES CSM_APPLICATION (APPLICATION_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_PROTECTION_ELEMENT ADD CONSTRAINT FK_PE_APPLICATION 
FOREIGN KEY (APPLICATION_ID) REFERENCES CSM_APPLICATION (APPLICATION_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_PROTECTION_GROUP ADD CONSTRAINT FK_PG_APPLICATION
FOREIGN KEY (APPLICATION_ID) REFERENCES CSM_APPLICATION (APPLICATION_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_PROTECTION_GROUP ADD CONSTRAINT FK_PROTECTION_GROUP 
FOREIGN KEY (PARENT_PROTECTION_GROUP_ID) REFERENCES CSM_PROTECTION_GROUP (PROTECTION_GROUP_ID);

ALTER TABLE CSM_PG_PE ADD CONSTRAINT FK_PROTECTION_ELEMENT_PROTECTION_GROUP 
FOREIGN KEY (PROTECTION_ELEMENT_ID) REFERENCES CSM_PROTECTION_ELEMENT (PROTECTION_ELEMENT_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_PG_PE ADD CONSTRAINT FK_PROTECTION_GROUP_PROTECTION_ELEMENT 
FOREIGN KEY (PROTECTION_GROUP_ID) REFERENCES CSM_PROTECTION_GROUP (PROTECTION_GROUP_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_ROLE ADD CONSTRAINT FK_APPLICATION_ROLE 
FOREIGN KEY (APPLICATION_ID) REFERENCES CSM_APPLICATION (APPLICATION_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_ROLE_PRIVILEGE ADD CONSTRAINT FK_PRIVILEGE_ROLE 
FOREIGN KEY (PRIVILEGE_ID) REFERENCES CSM_PRIVILEGE (PRIVILEGE_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_ROLE_PRIVILEGE ADD CONSTRAINT FK_ROLE
FOREIGN KEY (ROLE_ID) REFERENCES CSM_ROLE (ROLE_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_GROUP ADD CONSTRAINT FK_USER_GROUP 
FOREIGN KEY (USER_ID) REFERENCES CSM_USER (USER_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_GROUP ADD CONSTRAINT FK_UG_GROUP 
FOREIGN KEY (GROUP_ID) REFERENCES CSM_GROUP (GROUP_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_GROUP_ROLE_PG ADD CONSTRAINT FK_USER_GROUP_ROLE_PROTECTION_GROUP_GROUPS 
FOREIGN KEY (GROUP_ID) REFERENCES CSM_GROUP (GROUP_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_GROUP_ROLE_PG ADD CONSTRAINT FK_USER_GROUP_ROLE_PROTECTION_GROUP_ROLE 
FOREIGN KEY (ROLE_ID) REFERENCES CSM_ROLE (ROLE_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_GROUP_ROLE_PG ADD CONSTRAINT FK_USER_GROUP_ROLE_PROTECTION_GROUP_PROTECTION_GROUP 
FOREIGN KEY (PROTECTION_GROUP_ID) REFERENCES CSM_PROTECTION_GROUP (PROTECTION_GROUP_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_GROUP_ROLE_PG ADD CONSTRAINT FK_USER_GROUP_ROLE_PROTECTION_GROUP_USER 
FOREIGN KEY (USER_ID) REFERENCES CSM_USER (USER_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_PE ADD CONSTRAINT FK_PE_USER 
FOREIGN KEY (USER_ID) REFERENCES CSM_USER (USER_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_USER_PE ADD CONSTRAINT FK_PROTECTION_ELEMENT_USER 
FOREIGN KEY (PROTECTION_ELEMENT_ID) REFERENCES CSM_PROTECTION_ELEMENT (PROTECTION_ELEMENT_ID)
ON DELETE CASCADE;

ALTER TABLE CSM_MAPPING ADD CONSTRAINT FK_MAP_APPLICATION 
FOREIGN KEY (APPLICATION_ID) REFERENCES CSM_APPLICATION (APPLICATION_ID) ON DELETE CASCADE;

ALTER TABLE CSM_MAPPING
ADD CONSTRAINT UQ_MP_OBJ_NAME_ATTRI_NAME_APP_ID UNIQUE (OBJECT_NAME,ATTRIBUTE_NAME,APPLICATION_ID);
