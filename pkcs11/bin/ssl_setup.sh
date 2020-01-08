#!/usr/bin/env bash
###############################################################################
####
#### See: https://docs.datastax.com/en/dse/5.1/dse-admin/datastax_enterprise/security/secSetUpSSLCert.html
####
###############################################################################

#set fail if any command fails, and no undefined vars
set -eu

NODES=$@

TARGET="target/keystores"
PASS="123456"
STORE_TYPE="PKCS12"
STORE_EXT=".p12"

CLUSTER="mycluster"
ORG="datastax"

mkdir -p $TARGET

# Create root CA certificate
#1.create your own root CA for signing node certificates

openssl req -config src/main/resources/gen_rootCa_cert.conf \
-new -x509 -nodes \
-subj /CN=rootCa/OU=mycluster/O=DataStax/C=US/ \
-keyout $TARGET/rootCa.key \
-out $TARGET/rootCa.crt \
-days 36500


openssl x509 -in $TARGET/rootCa.crt -text -noout


#2.Create a single truststore

keytool -keystore $TARGET/dse-truststore${STORE_EXT} \
-storetype $STORE_TYPE \
-alias rootCa -importcert -file "$TARGET/rootCa.crt" \
-keypass $PASS \
-storepass $PASS \
-noprompt


keytool -list \
-storetype $STORE_TYPE \
-keystore $TARGET/dse-truststore${STORE_EXT} \
-storepass $PASS


#For each node in the cluster, create a keystore and key pair, and certificate signing request using FQDN of the node.

for node in $NODES;
do


DNAME="CN=$node, OU=$CLUSTER, O=$ORG, C=US"
KS=$TARGET/$node${STORE_EXT}
CSR=$TARGET/$node.csr
CSR_SIGNED="$CSR.signed"

echo "==> Generate truststore for $node"

keytool -genkeypair -keyalg RSA \
-alias $node \
-storetype $STORE_TYPE \
-keystore $KS \
-storepass $PASS \
-keypass $PASS \
-validity 36500 \
-keysize 2048 \
-dname "$DNAME"


keytool -list -storetype $STORE_TYPE -keystore $KS -storepass $PASS


echo "==> Generate certificate signing request for $node"

keytool -keystore $KS \
-alias $node \
-certreq -file $CSR \
-keypass $PASS \
-storepass $PASS \
-dname "$DNAME"


echo "==> Selfsign certificate for $node"

openssl x509 -req -CA "$TARGET/rootCa.crt" \
-CAkey "$TARGET/rootCa.key" \
-in $CSR \
-out $CSR_SIGNED \
-days 36500 \
-CAcreateserial \
-passin pass:$PASS


#For each node in the cluster, import the signed certificates into the keystores:
echo "==> Importing signed certificate"
keytool -keystore $KS \
-alias rootCa \
-importcert -file "$TARGET/rootCa.crt" \
-noprompt  -keypass $PASS \
-storepass $PASS



#Import the node's signed certificate into corresponding keystore
echo "==> Importing signed certificate"

keytool -keystore $KS \
-alias $node \
-importcert -noprompt \
-file $CSR_SIGNED \
-keypass $PASS \
-storepass $PASS

done


echo "==> Done"