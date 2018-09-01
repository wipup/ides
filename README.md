# IDES

## 1. Required software

Write later

## 2. How to setup

Write later

### Setup JVM system property

Please refer to file [JVM-Property.MD](JVM-Property.MD)

### Setup required property files
  - database configuration property file [sample-db-config.properties](sample-db-config.properties)
  - application configuration property file [sample-ides-config.properties](sample-ides-config.properties)

### Setup database

For SQL commands, please refer to file [database.sql.MD](database.sql.MD)


## 3. 

Write later


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
