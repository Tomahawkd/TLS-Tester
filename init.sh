#!/bin/bash

git submodule init
git submodule update

cd ./TLS-Attacker
mvn clean install