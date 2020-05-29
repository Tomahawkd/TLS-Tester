# TLS Tester

[![Build Status](https://travis-ci.org/Tomahawkd/TLS-Tester.svg?branch=master)](https://travis-ci.org/Tomahawkd/TLS-Tester)
[![CodeFactor](https://www.codefactor.io/repository/github/tomahawkd/tls-tester/badge)](https://www.codefactor.io/repository/github/tomahawkd/tls-tester)

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

## Install

1. Download `TLS-Tester-X.X.X.jar` from 
[release page](https://github.com/Tomahawkd/TLS-Tester/releases).

2. Prepare [Censys](https://censys.io/account/api) api id and secret 
and [Shodan](https://account.shodan.io) api key.  Then run `create_keys.sh` for 
key initialization and move them to `./keys`. 

3. Download testssl project from [here](https://github.com/drwetter/testssl.sh). You
need to put the whole folder into the same directory as the jar file. Or you can 
specific the testssl location using `--testssl`.

4. Create directory `./extensions` if you have extensions. Move your extensions into
the directory. Or you can specific the extension dir location using `--extension`.

5. Run jar file you downloaded. Usage is as follows.

(Note: When using testssl on MacOS, you need install `coreutil` via `brew` on MacOS 
due to there is no `timeout` command on MacOS. The testssl will fall if the command 
is missing. You also need to link `gtimeout` in `coreutil` to `timeout` using either 
`alias` or `ln`)

## Usage

```
Usage: <main class> [options] <Type>::<Target String> 
Available format: 
      shodan[::<start>-<end>]::<query>, file::<path>, ips::<ip>[;<ip>], socket::[<ip>[:<port>]]
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
    --net_thread
      Total network thread for data process to be activated.
      Default: 5

```

## Build

If you want to build it yourself, you can follow the instructions below.

Building the project requires maven.

1. Clone the project into your device, and enter the directory.

2. mvn package.

3. You'll get executable and extensions in `./apps/` and `./apps/extensions` respectively.
API files are located in `./apis`.

(Note: If Stack Overflow error occurs, please enlarge your stack size by configuring env 
`MAVEN_OPTS=-Xss2m`)

## Key Workflow Phrases

- Host Acquire Phrase
  - Data acquire from files, Shodan query, command line argument and socket as a slave 
  (implement class `TargetSource` in data api)
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

