# TLS Tester

[![Build Status](https://travis-ci.org/Tomahawkd/TLS-Tester.svg?branch=v2.0)](https://travis-ci.org/Tomahawkd/TLS-Tester)

```
 ______  __       ____            ______                __                   
/\__  _\/\ \     /\  _`\         /\__  _\              /\ \__                
\/_/\ \/\ \ \    \ \,\L\_\       \/_/\ \/    __    ____\ \ ,_\    __   _ __  
   \ \ \ \ \ \  __\/_\__ \   _______\ \ \  /'__`\ /',__\\ \ \/  /'__`\/\`'__\
    \ \ \ \ \ \L\ \ /\ \L\ \/\______\\ \ \/\  __//\__, `\\ \ \_/\  __/\ \ \/ 
     \ \_\ \ \____/ \ `\____\/______/ \ \_\ \____\/\____/ \ \__\ \____\\ \_\ 
      \/_/  \/___/   \/_____/          \/_/\/____/\/___/   \/__/\/____/ \/_/
                                                                             
A TLS channel security tester by Tomahawkd@Github
For more information please visit https://github.com/Tomahawkd/TLS-Tester
Thanks to http://patorjk.com/software/taag for Console ASCII art
```

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

(Note: If Stack Overflow error occurs, please enlarge your stack size by configuring env 
`MAVEN_OPTS=-Xss2m`)

(Note2: With API packages, you could develop extensions for TLS-Tester, move them to 
`./extensions` to load these extensions or specific directories by `--extension <dir>`
 in command line)

## Usage

```
Usage: <main class> [options] <Type>::<Target String> 
Available format: 
      shodan[::<start>-<end>]::<query>, file::<path>, ips::<ip>[;<ip>]
  Options:
    --db
      Database name.
      Default: tlstester
    --db_pass
      Database password (if any).
      Default: <empty string>
    --db_type
      Database type(sqlite etc.).
      Default: sqlite
    --db_user
      Database username (if any).
      Default: <empty string>
    --debug
      Show debug output (sets logLevel to DEBUG)
      Default: false
    -e, --enable_cert
      enable searching and testing other host has same cert. It will be a long 
      tour. 
      Default: false
    --extension
      manually set extensions' directory
      Default: extensions/
    -h, --help
      Prints usage for all the existing commands.
    --quiet
      No output (sets logLevel to NONE)
      Default: false
    --safe
      Ignore all extensions.
      Default: false
    --temp
      Temp file expired day. (-1 indicates forever)
      Default: 7
    --testssl
      Testssl path. (No slash at the end)
      Default: ./testssl.sh
    -t, --thread
      Total thread to be activated.
      Default: 5

```

## Key Workflow Phrases

- Host Acquire Phrase
  - Data acquire from files, Shodan query, command line argument (implement class 
  `TargetProvider`, not exposed yet)
  - Host with the same certificate as the testing host acquire from Censys (activate 
  by `-e` in command line)

- Data Collect Phrase (implement class `DataCollector` in data api)
  - Shodan Host data
  - Testssl test result (Data structure in testssl-bridge)
  - Device Identification (for device brand detection, implement class `Identifier` 
  in identifier api)

- Analyze Phrase (implement class `Analyzer` in analyzer api)
  - Three Attack tree analyzers (Leaky, Tainted, Partially Leaky)

- Recording Phrase (implement class `RecorderDelegate` in database api)
  - Use database to store results

