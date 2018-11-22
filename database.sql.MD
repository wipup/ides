# How to setup the database 


# 1. Update/Alter the existing database schema
This section is for the original purpsoe of this project; improve the old existing one.

## 1.1. Client and organisation table
- Create Sequence that will be used to generate primary keys for this table.
- The added Sequence must start with the highest existing primary key plus 1 (2706 + 1 = 2707).
- No further alteration needed for table CONTACTS.

```
CREATE SEQUENCE contacts_sequence INCREMENT BY 1 START WITH 2707 NOMAXVALUE NOCYCLE;
```


## 1.2. Project idea table
- Add 2 new columns to the IDEAS2 table.
- Create Sequence that will be used to generate primary keys for this table.
- The added Sequence must start with the highest existing primary key plus 1 (1287 + 1 = 1288).
- Add default values to the new columns.

```
ALTER TABLE IDEAS2 MODIFY (PROJORG decimal(11,0) CONSTRAINT PROJORG_NOT_NULL_CONSTRAINT NOT NULL); 
ALTER TABLE IDEAS2 ADD PROJTYPE varchar2(1);
ALTER TABLE IDEAS2 ADD PROJCREATE TIMESTAMP;

UPDATE IDEAS2 SET PROJTYPE = 'N' WHERE PROJTYPE IS NULL;
UPDATE IDEAS2 SET PROJCREATE = PROJDATE;

CREATE SEQUENCE ideas2_sequence INCREMENT BY 1 START WITH 1288 NOMAXVALUE NOCYCLE;
```

## 1.3. Contact Profile table
 - Create a table for Contact Profile
 - Create Sequence that will be used to generate primary keys for Contact Profile.
 
```
CREATE TABLE CONTACT_PROFILE
(
	ID decimal(11,0) PRIMARY KEY NOT NULL,
	ADDR varchar2(100),
	DOES varchar2(50),
	NAME varchar2(50),
	POSTCODE varchar2(10),
	TEL varchar2(20),
	CONTACT varchar2(50),
	EMAIL varchar2(50),
	OWNER varchar2(100) NOT NULL,
	OWNERTYPE varchar2(1)
);
CREATE INDEX CONTACT_PROFILE_OWNER_INDEX ON CONTACT_PROFILE(OWNER);
CREATE SEQUENCE contact_profile_sequence INCREMENT BY 1 START WITH 1 NOMAXVALUE NOCYCLE; 
```


# 2. Create a new database schema from scratch
This section is for those who want to setup the database from scratch in order to test, play around or whatever.

## 2.1 Create new user
Firstly, access the oracle database with Database Administrator permission (sysdba) and create a new user.
Don't forget to change _<your_username>_ and _<your_password>_  to your preferences.

```
sqlplus / as sysdba
CREATE USER <your_username> IDENTIFIED BY <your_password>;
GRANT ALL PRIVILEGES TO <your_username>;
```


## 2.2 Create Client and Organisation table
 - Create a table for client & organisation
 - Create Sequence that will be used to generate primary keys for this table

```
CREATE TABLE CONTACTS
(
    ORGADDR varchar2(100),
    ORGCONTACT varchar2(50),
    ORGDOES varchar2(50),
    ORGEMAIL varchar2(50),
    ORGID decimal(11,0) PRIMARY KEY NOT NULL,
    ORGNAME varchar2(50),
    ORGPOSTCODE varchar2(10),
    ORGTEL varchar2(20)
);
CREATE UNIQUE INDEX PRIMARY_CONTACTS_INDEX ON CONTACTS(ORGID);
CREATE SEQUENCE contacts_sequence INCREMENT BY 1 START WITH 1 NOMAXVALUE NOCYCLE;
```


## 2.3 Create Project Idea table
 - Create a table for project ideas
 - Create Sequence that will be used to generate primary keys for this table
 - Create a foreign key to client & organisation table. 
 
```
CREATE TABLE IDEAS2
(  PROJAIMS clob,
   PROJAPPROVED varchar2(1),
   PROJDATE timestamp,
   PROJDELIVER clob,
   PROJID decimal(11,0) PRIMARY KEY NOT NULL,
   PROJORG decimal(11,0) NOT NULL,
   PROJQUEST clob,
   PROJSTUDENT varchar2(100),
   PROJTITLE varchar2(100),
   PROJTYPE varchar2(1),
   PROJCREATE timestamp
);
ALTER TABLE IDEAS2 ADD CONSTRAINT IDEAS_ORG FOREIGN KEY (PROJORG) REFERENCES CONTACTS(ORGID);
CREATE UNIQUE INDEX IDEAS2_PRIMARY_INDEX ON IDEAS2(PROJID);
CREATE SEQUENCE ideas2_sequence INCREMENT BY 1 START WITH 1 NOMAXVALUE NOCYCLE;
```


## 2.4 Create Contact Profile table
This section is the same as section 1.3. Please follow the instruction in section 1.3.

