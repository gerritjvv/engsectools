# Overview

Java JCA abstracts cryptographic implementations as Providers.
Providers can implement any or all of the cryptographic services specified by JCA, e.g  Signature, MessageDigest, Cipher, Mac, KeyStore,

For more information on JCA cryptographic services see 
https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html


Cryptographic Providers implement any number of cryptographic algorithms. JCA services are instantiated by calling the "getInstance(crypto-algo-name)"
on any of the service classes (e.g Signature.getInstance("SHA-256")), the Provider database will search for the first provider that implements
the algorithm for that service and return it. Alternatively service classes can be instantiated with specific providers using "getInstance(cryto-algo-name, provider-name)"


The Keystore service is used to store and retreive passwords, private+public keys and certificates.
Several keystore types are available like jks, jceks, PKCS12, PKCS11, for software PKCS12 (RSA Personal Information Exchange syntax format) is recommended and it is the default keystore in java 1.9,
PKCS11 adds support for hardware cryptographic tokens. 


PKCS11 is a specification and API by RSA/OASSIS for cryptographic tokens.
Used for HSM (hardware security modules) and smart cards.

The Sun PKCS#11 Provider is a bridge/proxy between the Java JCA/JCE apis and the natice PKCS11 api.

The PKCS11 api implementation must be installed as either a shared object library (.so) or dynamic link library (.ddl),
prior to using it in the JVM.

To use PKCS11 as a keystore:

1. Install hardware module and its native PKCS#11 .so/.ddl libraries.

2. Configure Sun PKCS#11 bridge provider

2. check that it is avilable using "Security.getAlgorithms("KeyStore")" and 
load using Keystore.getInstance("PKCS#11")



# Exmaple implemenation

Use https://www.opendnssec.org/softhsm/ as a software PKCS#11 implementation as hardware can be expensive.

1. Build

run the `./build.sh` command.

Then log into the docker instance using `docker run -it engsecks  bash`


2. Configure the PKCS#11 Provider 

Init the pkcs11 slot 0

softhsm2-util --init-token --slot 0 --label "My token 1" --pin 1234 --so-pin 1234

a. java.security

Locate java.security using ```find / -iname "java.security"```

Then add in the line (after the last security.provider entry)

```
 security.provider.N=sun.security.pkcs11.SunPKCS11 /etc/softhsm/pkcs11.cfg

```

 **Important:**
   While testing with java 1.8, providers must be incremented 1,2,3,4 in order, if you add the security provider to 1,2,3 and then add as 5 
   it will not be read, and java wants it to be 4.
 

b. Create the file ```/etc/softhsm/pkcs11.cfg```

Locate the libsofthsm2.so object file using ```find / -iname "*hsm*.so"```

And add to pkcs11.cfg

```
name = SoftHSM
library = /usr/lib/x86_64-linux-gnu/softhsm/libsofthsm2.so
slotListIndex = 0
```

Note: slot depends on the slot used in the softhsm2-util token creation
 if slotListIndex 1 was used change to slotListIndex=1


c. Use as KeyStore 

Run the example application using:

```
java -jar /opt/engsec/engsec.jar
```

And then use the java keytool to list the private key created

```
keytool -list -storetype "PKCS11"
```

# SSL

For docs on setting up Datastax SSL see: https://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/security/secSetUpSSLCert.html

## Utils

**bin/ssl_setup.sh**

Creates self signed certificates imported into PKCS12 truststores as per the `secSetUpSSLCert.html` online help.

E.g

```
bin/ssl_setup.sh ip-10-200-181-139.datastax.lan ip-10-200-181-141.datastax.lan ip-10-200-181-142.datastax.lan
```

Creates

```
⇒  ls -lh target/keystores
total 128
-rw-r--r--  1 gvanvuuren  staff   1.0K Jan 20 01:26 dse-truststore.p12
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 ip-10-200-181-139.datastax.lan.csr
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 ip-10-200-181-139.datastax.lan.csr.signed
-rw-r--r--  1 gvanvuuren  staff   4.3K Jan 20 01:26 ip-10-200-181-139.datastax.lan.p12
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 ip-10-200-181-141.datastax.lan.csr
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 ip-10-200-181-141.datastax.lan.csr.signed
-rw-r--r--  1 gvanvuuren  staff   4.3K Jan 20 01:26 ip-10-200-181-141.datastax.lan.p12
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 ip-10-200-181-142.datastax.lan.csr
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 ip-10-200-181-142.datastax.lan.csr.signed
-rw-r--r--  1 gvanvuuren  staff   4.3K Jan 20 01:26 ip-10-200-181-142.datastax.lan.p12
-rw-r--r--  1 gvanvuuren  staff   1.1K Jan 20 01:26 rootCa.crt
-rw-r--r--  1 gvanvuuren  staff   1.7K Jan 20 01:26 rootCa.key
-rw-r--r--  1 gvanvuuren  staff    17B Jan 20 01:26 rootCa.srl
```
