FROM ubuntu:18.04

ENV TESTER_VERSION=3.2.2

RUN apt-get update && apt-get upgrade -y && \
    apt-get install openjdk-8-jre git curl -y

## add user
RUN addgroup tlstester && \
    useradd -g tlstester tlstester && \
    echo "tlstester:password" | chpasswd

# make directories
WORKDIR /home/tlstester/
RUN mkdir ./extensions && \
    mkdir ./keys && \
    mkdir ./testssl.sh

WORKDIR /home/tlstester/testssl.sh
RUN mkdir ./etc && mkdir ./bin

# create entry
WORKDIR /home/tlstester/
RUN echo "java -jar TLS-Tester-${TESTER_VERSION}-with-deps.jar \$@" > docker_run.sh
RUN chmod +x docker_run.sh

# make them all belongs to user tlstester
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

ENTRYPOINT ./docker_run.sh

CMD ["--help"]