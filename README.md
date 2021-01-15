# TLS Tester

[![Build Status](https://travis-ci.org/Tomahawkd/TLS-Tester.svg?branch=master)](https://travis-ci.org/Tomahawkd/TLS-Tester)
![Java CI with Maven](https://github.com/Tomahawkd/TLS-Tester/workflows/Java%20CI%20with%20Maven/badge.svg)
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
TLS-Tester is a generic SSL/TLS security scanner with plugin system for extensive 
usage. It has several highly extensible APIs for customized security testing.

My report related to this project. [Download](
https://github.com/Tomahawkd/TLS-Tester/releases/download/paper.rev1/unsecure.tls.channel.chn.pdf)

TLS-Tester has three internal analysis procedure for SSL/TLS unsecured channels 
referred from [Postcards from the Post-HTTP World: 
Amplification of HTTPS Vulnerabilities in the Web Ecosystem](
https://ieeexplore.ieee.org/document/8835223).

Note: Currently due to testssl runs on *nix platform, Windows are not available, 
I'm considering using WSL as alternative for testssl.

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
`alias` or `ln`. If you want to get rid of this, use `--testssl_no_timeout` to disable.)

## Usage

```
Usage: <main class> [options] <Type>::<Target String> 
Available format: 
      shodan[::<start>-<end>]::<query>, file::<path>, ips::<ip>[;<ip>], 
      socket::[<ip>[:<port>]] 
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
    --net_thread
      Total network thread for data process to be activated.
      Default: 5
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
    --testssl_no_timeout
      Disable testssl timeout in connection
      Default: false
    -t, --thread
      Total thread to be activated.
      Default: 5

```

## Build

If you want to build it yourself, you can follow the instructions below.

Building the project requires maven.

1. Clone the project into your device, and enter the directory.

2. `git submodule init`, `git submodule update` and `mvn package`.

3. You'll get executable and extensions in `./apps/` and `./apps/extensions` respectively.
API files please refer [TLS-Tester-api](https://github.com/Tomahawkd/TLS-Tester-api).

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

