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


## 4. Fix expired certificate in Glassfish

The only way of fixing this problem is simple, delete the expired certificate.

  1. Find the certificate file.
  
  Glassfish's certificate file is cacerts.jks which is a Java's keystore file and normally located in /glassfish/domains/domain1/config/
    
  2. Use Java's keytool to list the certificates and their alias name
  
```
keytool -v -list -keystore ./cacerts.jks
```

  The default password of Glassfish's keystore is changeit
    
  3. Delete the expired certificate using its alias name
  
```
keytool -delete -keystore ./cacerts.jks -alias aliasname
```

  E.g. the Equifax Secure Certificate Authority which is just expired on 22nd August 17:41:51 BST 2018
    
  It's alias name is equifaxsecureca
    
```
keytool -delete -keystore ./cacerts.jks -alias equifaxsecureca
```
