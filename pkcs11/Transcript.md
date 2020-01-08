
# Securing DSE internode incryption vial SSL and PKCS11

##launch a 3 node cluster

```
ctool --provider=openstack launch --platform=xenial gerritfuzz 3
```

##install DSE 5.1.6 on all nodes
```
ctool install -v 5.1.6 gerritfuzz enterprise
```

## generate the ssl internode security config and setup
```
ctool secure -i gerritfuzz
```

##install libsofthsm2
```
ctool run gerritfuzz 0,1,2 "sudo apt-get install -y softhsm2 libsofthsm2-dev softhsm2 softhsm2-common"
```

## setup java.security to load PKCS11 provider

```
ctool run gerritfuzz 0,1,2 "sudo sed -i '/security.provider.9/{n;s/.*/security.provider.10=sun.security.pkcs11.SunPKCS11 \/etc\/softhsm\/pkcs11.cfg/}' /usr/lib/jvm/jdk1.8.0_40/jre/lib/security/java.security"

```

## configure java PKCS11 config

```
ctool run gerritfuzz 0,1,2 'sudo mkdir -p /etc/softhsm && printf "name = SoftHSM\nlibrary = /usr/lib/x86_64-linux-gnu/softhsm/libsofthsm2.so\nslotListIndex = 0" > /etc/softhsm/pkcs11.cfg'
```

Ensure the /var/lib/softhsm/tokens file is "cassandra" user readable

ctool run gerritfuzz 0,1,2 'sudo mkdir -p /var/lib/softhsm/tokens && sudo chmod -R 777 /var/lib/softhsm/tokens'


## Setup DSE secure config and key tool

```ctool secure -i gerritfuzz```

This generates /var/tmp/.keystore and /var/tmp/.truststore

## Import .keystore into pkcs11 on each node

note the passwords are 'ctool_keystore' and 'ctool_trustore'


NOTE:

keytool -importkeystore -srckeystore /var/tmp/.keystore -deststoretype PKCS11 -srcstorepass ctool_keystore -deststorepass 123456 -srcalias node0 -destalias ip-10-200-176-133.datastax.lan
keytool -importkeystore -srckeystore /var/tmp/.keystore -deststoretype PKCS11 -srcstorepass ctool_keystore -deststorepass 123456 -srcalias node1 -destalias ip-10-200-176-134.datastax.lan
keytool -importkeystore -srckeystore /var/tmp/.keystore -deststoretype PKCS11 -srcstorepass ctool_keystore -deststorepass 123456 -srcalias node2 -destalias ip-10-200-176-135.datastax.lan


```
for i in {0..2}; do \
 ctool run gerritfuzz $i \
  "keytool -importkeystore -noprompt \
  -srckeystore /var/tmp/.keystore -srcstoretype JKS \
  -deststoretype PKCS11 \
  -srcstorepass ctool_keystore -deststorepass 123456 \
  -srcalias node$i -destalias node$i"; \
done
```

## Check that the import was successfull

```
for i in {0..2}; do \
 ctool run gerritfuzz $i "keytool -list -storepass 123456 -storetype PKCS11"; \
done
```

