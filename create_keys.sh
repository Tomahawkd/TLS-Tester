#!/bin/bash

# censys
echo -n "Enter your Censys Api ID: "
read -r API_ID
echo ${API_ID} >> ./censys_key
echo -n "Enter your Censys Api Secret: "
read -r SECRET
echo ${SECRET} >> ./censys_key

# shodan
echo -n "Enter your Shodan Api Key: "
read -r API_KEY
echo ${API_KEY} >> ./shodan_key