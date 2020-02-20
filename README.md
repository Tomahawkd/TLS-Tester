# TLS Tester

[![Build Status](https://travis-ci.org/Tomahawkd/TLS-Tester.svg?branch=v1.0)](https://travis-ci.org/Tomahawkd/TLS-Tester)

## Introduction
This project is based on [Postcards from the Post-HTTP World: 
Amplification of HTTPS Vulnerabilities in the Web Ecosystem](
https://ieeexplore.ieee.org/document/8835223) 
which is studying on SSL/TLS vulnerable implementation and mis-usage 
among the current Internet environment.  
I implement the attack tree described in the paper for vulnerable SSL/TLS
channels detection.

## Bootstrap
1. The project is built using maven for dependencies management. You may need to 
install maven.

2. Prepare [Censys](https://censys.io/account/api) api id and secret 
and [Shodan](https://account.shodan.io) api key. These keys will be required in the 
next step.

3. Run `init.sh` for project initialization. You will be asked for censys and shodan api 
keys. Or you can edit your key in directory `/apps/keys`.

4. mvn package && Run project in `/apps`!

## Workflow

```
input (File or Shodan contains ip parsed as List) foreach
    -> testssl (Generates json results)
    -> Analyzer (Attack tree detector implementation)
          | -> TLS-Attacker
          | -> Censys
          | -> testssl (refer json results)
    -> Record (Sqlite)
-> Statistic (Sqlite)

```

## TODO List

1. Stream IP target provider
2. Distributed tester (Master & Slave)
3. Extendable interface for generic testing
4. Optimize testssl json result parser
5. Database management
