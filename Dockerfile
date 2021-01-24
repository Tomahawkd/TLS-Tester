FROM ubuntu:18.04

ENV TESTSSL_VERSION=3.0.4
ENV TESTER_VERSION=3.2.2

RUN apt-get update && apt-get upgrade -y && \
    apt-get install openjdk-8-jre git curl -y

RUN addgroup tlstester && \
    useradd -g tlstester tlstester && \
    echo "tlstester:password" | chpasswd

WORKDIR /home/tlstester/
RUN mkdir ./extensions && \
    mkdir ./keys && \
    mkdir ./testssl.sh

WORKDIR /home/tlstester/testssl.sh
RUN mkdir ./etc && mkdir ./bin
WORKDIR /home/tlstester/
RUN chown -R tlstester /home/tlstester

USER tlstester
COPY --chown=tlstester:tlstester ./testssl.sh/etc/. /home/tlstester/testssl.sh/etc/
COPY --chown=tlstester:tlstester ./testssl.sh/bin/. /home/tlstester/testssl.sh/bin/
COPY --chown=tlstester:tlstester ./testssl.sh/testssl.sh  /home/tlstester/testssl.sh/
COPY --chown=tlstester:tlstester ./testssl.sh/openssl-iana.mapping.html  /home/tlstester/testssl.sh/

COPY --chown=tlstester:tlstester ./apps/TLS-Tester-${TESTER_VERSION}-with-deps.jar /home/tlstester/
COPY --chown=tlstester:tlstester ./apps/extensions/. /home/tlstester/extensions/
COPY --chown=tlstester:tlstester shodan_key /home/tlstester/keys/
COPY --chown=tlstester:tlstester censys_key /home/tlstester/keys/

ENTRYPOINT java -jar TLS-Tester-${TESTER_VERSION}-with-deps.jar

CMD ["--help"]