# TLS Tester

## Introduction
This project is based on [Postcards from the Post-HTTP World: 
Amplification of HTTPS Vulnerabilities in the Web Ecosystem](
https://ieeexplore.ieee.org/document/8835223) 
which is studying on SSL/TLS vulnerable implementation and mis-usage 
among the current Internet environment.

This project is to re-implements their works in our approach.

## Setting up
1. The project is built using maven for dependencies management. You may need to 
install maven.

2. Prepare [Censys](https://censys.io/account/api) api id and secret 
and [Shodan](https://account.shodan.io) api key. These keys will be required in the 
next step.

3. Run `init.sh` for project initialization. You will be asked for censys and shodan api 
keys. Or you can edit your key in directory `/keys`.

4. Run project!
