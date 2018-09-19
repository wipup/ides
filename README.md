# IDES

## 1. Required software

There are 4 required technologies.

- [Java SE 1.8 or newer](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [GlassFish 5.0 or newer](https://javaee.github.io/glassfish/download)
  - Other fully-fledged Java EE servers should probably be fine but none of them have been tested.
- [Oracle Database 10g or newer](https://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html)
  - If use a version newer than 10g, the SQL statement selectIdeasForListing 
  [idea-mapper.xml](src/main/resources/resources/mybatis/mapper/idea-mapper.xml) 
  should be updated to use a new keyword for paging (`FETCH NEXT # ROWS ONLY`) instead of using the keyword `ROWNUM`. 
  The Java method [LazyIdeaDataModel.load](/src/main/java/ports/soc/ides/controller/helper/LazyIdeaDataModel.java) 
  may need to be changed too.
- [Apache Maven](https://maven.apache.org/download.cgi)
  - Recommend the latest version.
  - Not sure which version is the minimum version. 

## 2. How to setup

### 2.1. Compile the source code using Maven command

```
mvn package
```

If the compilation succeeds, there should be a file `ides.war`. Keep this file. It will be used to deploy in step 3.

### 2.2. If the compilation fails, it may be because it cannot download Oracle driver jar files.

There are three ways to fix this. The first one is find the jar file from other source and add it to the Maven local repository manually.
The second one is following the instruction of [how to download the file from Oracle Maven repository](https://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9016)
This method will need to create a setting file. The example of setting file is [here](sample-maven-settings.xml).

The last method is to edit the [pom.xml](pom.xml) and replace the dependency driver with the version that you have.
 - Comment line 92-98 out (the Oracle driver dependency)
 - Create a new dependecy about the Oracle Driver that you have.

### 2.3. Setup database

Please read the instruction in [database.sql.MD](database.sql.MD)

### 2.4. Prepare IDES initial configuration file and datasource file

#### The example of the required property files
  - database configuration property file [sample-db-config.properties](sample-db-config.properties)
  - application configuration property file [sample-ides-config.properties](sample-ides-config.properties)

Don't forget to chage the properties in both files.

The properties that should be changed in the configuration file are
  - ides.role.administrator.email
  - ides.role.staff.emailDomain
  - ides.role.student.emailDomain
  - ides.externalApi.googleSignIn.clientId

### 2.5. Setup GlassFish JVM system property

First thing first, find the `asadmin` file. This file is for accessing the administrator console of GlassFish. 
It should be located in folder `/bin`

IDES requires 4 JVM system properties. For other environment, please refer to file [JVM-Property.MD](JVM-Property.MD)

```
./bin/asadmin
start-domain domain1
create-system-properties ides.configuration.initial.fileLocation=/path/to/your/ides-config.properties
create-system-properties ides.configuration.database.fileLocation=/path/to/your/log/directory/
create-system-properties ides.log.outputLocation=/path/to/your/db-config.properties
```

The last system property is as follow. This one is fixed. No need to change anything.

```
create-system-properties log4j.configurationFactory=ports.soc.ides.logger.LoggerConfigurationFactory
```

If you are using Linux, you may face a Java security bug. To fix it, add more property as follow.

```
create-system-properties java.security.egd=file\\:///dev/urandom
```

## 3. How to deploy

First, enter asadmin console and start the GlassFish server.
```
./bin/asadmin
start-domain domain1
```

Then, deploy the file. This step needs the war file in step 2.1.
For example, if the war file is `ides.war`.
```
deploy /home/user/full/path/to/ides.war
```
Or
```
deploy C:/ides/ides.war
```

Then, after the deployment completed, the application should be accessible via [localhost](http://localhost:8080/ides)


## 4. Fix known bugs/issues

### 4.1 Method not found in GlassFish (Security-related method)
Solution taken from [Stackoverflow.com](https://stackoverflow.com/questions/49383650/sun-security-ssl-sslsessionimpl-not-found)

***Exception*** caused by java.lang.NoSuchMethodError: sun.security.ssl.SSLSessionImpl. or maybe other error that I cannot remember.

***Solution:*** downgrade the Java version to Java 1.8.0-151 or use other newer version of GlassFish


### 4.2 Expired certificate in GlassFish

Having error about expired certificate sometimes cause the logger (log4j2) to stop working.

One of the ways to fix this problem is to delete the expired certificates.

  1. Find the certificate file.
  
  GlassFish's certificate file is cacerts.jks which is a Java's keystore file and normally located in /glassfish/domains/domain1/config/
    
  2. Use Java's keytool to list the certificates and their alias name
  
```
keytool -v -list -keystore ./cacerts.jks
```

  The default password of GlassFish's keystore is changeit
    
  3. Delete the expired certificate using its alias name
  
```
keytool -delete -keystore ./cacerts.jks -alias aliasname
```

  E.g. the Equifax Secure Certificate Authority which is just expired on 22nd August 17:41:51 BST 2018
    
  It's alias name is *equifaxsecureca*
    
```
keytool -delete -keystore ./cacerts.jks -alias equifaxsecureca
```

  Current known expired *certificates* are *gtecybertrustglobalca* and *equifaxsecureca*.

### 4.3 Redirect from GlassFish's root context path to application context path

Normally, all applications are deployed and accessible using its own context path, for example, IDES is deployed on path http://localhost:8080/ides

If someone browses the url without application's context path (e.g. http://localhost:8080/), GlassFish will render its default index page which contains a link to server's administrator console and etc.

To make the page automatically redirect to the application page (/ides), modify the *index.html* file located in *\glassfish\domains\domain1\docroot*

The recommended way to do this is to use Javascript to redirect to the application page, by removing everything inside the *body* tag and add this code below instead.
  
```
<script type="text/javascript">
	window.location.href = '/ides';
</script>
```

Also, change the page title by editing text inside the <title> tag to *IDES Project Idea Database* to let users know that they are accessing the right application.

# 5. How to enable HTTPS on GlassFish 5.0
write later
