# IDES

## 1. Required software

1. Java SE 1.8 or newer
2. GlassFish 5.0
3. Oracle Database Express Edition 10g
4. Apache Maven 

## 2. How to setup

### 2.1. Compile the source code using Maven command

```
mvn package
```

### 2.2. If the compilation fails, it is because it cannot download Oracle driver jar files.

There are two ways to fix this. The first one is find the jar file and add the it to the Maven local repository manually.
The second one is following the instruction of [how to download the file from Oracle Maven repository](https://docs.oracle.com/middleware/1213/core/MAVEN/config_maven_repo.htm#MAVEN9016)
This method will need to create a setting file. The example of setting file is [here](sample-maven-settings.xml).

### 2.3. Setup database

Please read the file [database.sql.MD](database.sql.MD)

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

First thing first, find a file asadmin. This file is the administrator console of GlassFish. It should be located in folder /bin

IDES requires 4 JVM system properties. For other environment, please refer to file [JVM-Property.MD](JVM-Property.MD)

```
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





## 4. Fix known bugs/issues

### 4.1 Method not found in GlassFish (Security-related method)
Solution taken from [Stackoverflow.com](https://stackoverflow.com/questions/49383650/sun-security-ssl-sslsessionimpl-not-found)

***Exception*** caused by java.lang.NoSuchMethodError: sun.security.ssl.SSLSessionImpl.

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

### 4.3 Redirect from GlassFish's root context to application context

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
