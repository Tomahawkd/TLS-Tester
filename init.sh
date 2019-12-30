#!/bin/bash

# init submodule
tls="$(cut -d' ' -f1 <<< `ls ./TLS-Attacker | wc -l`)"
test="$(cut -d' ' -f1 <<< `ls ./testssl.sh | wc -l`)"
censys="$(cut -d' ' -f1 <<< `ls ./Censysjava | wc -l`)"

if [[ $tls -eq 0 -o $test -eq 0 -o $censys -eq 0 ]]; then
    git submodule init
    git submodule update
fi

## key dir
mkdir ./keys
cd ./keys
if [[ $? -eq 0 ]]; then

# censys
echo -n "Enter your Censys Api ID: "
read API_ID
echo ${API_ID} >> ./censys_key
echo -n "Enter your Censys Api Secret: "
read SECRET
echo ${SECRET} >> ./censys_key

# shodan
echo -n "Enter your Shodan Api Key: "
read API_KEY
echo ${API_KEY} >> ./shodan_key
fi
cd ../

## temp dir
mkdir ./temp
cd ./temp
mkdir ./censys
mkdir ./shodan
mkdir ./testssl