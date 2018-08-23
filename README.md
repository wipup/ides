# IDES




## Fix expired certificate in Glassfish
  1. Find the certificate file.
    Glassfish's certificate file is cacerts.jks which is normally located in /glassfish/domains/domain1/config/
  2. Use keytool to list the certificates and their alias name
  
```
keytool -v -list -keystore ./cacerts.jks
```

    The default password is changeit
    
  3. Delete the expired certificate using its alias name
  
```
keytool -delete -keystore ./cacerts.jks -alias aliasname
```

  E.g. the Equifax Secure Certificate Authority which is just expired on 22nd August 17:41:51 BST 2018
    
  It's alias name is equifaxsecureca
    
```
keytool -delete -keystore ./cacerts.jks -alias equifaxsecureca
```
