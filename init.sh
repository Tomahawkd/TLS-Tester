#!/bin/bash

domain="$(cut -d' ' -f1 <<< `ls ./Subdomain-Detect | wc -l`)"
tls="$(cut -d' ' -f1 <<< `ls ./TLS-Attacker | wc -l`)"
test="$(cut -d' ' -f1 <<< `ls ./testssl.sh | wc -l`)"

if [[ $domain -eq 0 -o $tls -eq 0 -o $test -eq 0 ]]; then
    git submodule init
    git submodule update
fi

cd ./TLS-Attacker
mvn clean install