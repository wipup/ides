# IDES

## 1. Required software

## 2. How to setup

## 3. 




## 4. Fix expired certificate in Glassfish

The only way of fixing this problem is simple, delete the expired certificate.

  1. Find the certificate file.
  
  Glassfish's certificate file is cacerts.jks which is normally located in /glassfish/domains/domain1/config/
    
  2. Use keytool to list the certificates and their alias name
  
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
