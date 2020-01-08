FROM ubuntu:16.04


RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections && \
    apt-get update && \
        apt-get install -y --no-install-recommends software-properties-common && \
        add-apt-repository ppa:webupd8team/java && \
        apt-get update && \
        apt-get install -y --no-install-recommends oracle-java9-installer && \
    rm -rf /var/lib/apt/lists/*

RUN apt-get update && \
    apt-get install -y vim && \
    apt-get install -y libsofthsm2 softhsm2 softhsm2-common opensc && \
    mkdir -p /var/lib/softhsm/tokens

RUN mkdir -p /opt/engsec

COPY . /opt/engsec/

RUN cp /opt/engsec/src/main/resources/gen_rootCa_cert.conf /opt/engsec/en_rootCa_cert.conf

RUN openssl req -config /opt/engsec/en_rootCa_cert.conf \
    -new -x509 -nodes \
    -subj /CN=rootCa/OU=cluster_name/O=DataStax/C=US/ \
    -keyout /opt/engsec/rootCa.key \
    -out /opt/engsec/rootCa.crt \
    -days 365 && \
    softhsm2-util --init-token --slot 0 --label "My token 1" --pin 1234 --so-pin 1234

ADD target/pkcs11-1.0-SNAPSHOT.jar /opt/engsec/engsec.jar

CMD java $JVM_OPTIONS -jar /opt/engsec/engsec.jar
