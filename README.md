Ozwillo portal implementation
===========================

[![Build Status](https://travis-ci.org/ozwillo/ozwillo-portal.svg?branch=master)](https://travis-ci.org/ozwillo/ozwillo-portal)

## Prerequisites

Building requires Java 8 or later.

"Unit" tests require a working MongoDB 4.2 database at localhost:27017. "Integration" tests require an Internet connection and generally that the Ozwillo ecosystem (kernel, data core, etc.) is set up and in the expected condition. They are therefore more brittle and for that reason, they do not fail the build (you should make sure they pass, though).


## Installation

* After cloning this repository, install the js packages

```
yarn install
```

* Configure your application's environment
  * Create an src/main/resources/application-dev.yml file (dev is the default profile)
  * Configure your own properties in it :
  
  Here is an example of properties you have to change in it :
````yaml
application:
  devmode: true
  production: false

web:
  home: https://www.ozwillo.com

kernel:
  base_uri: https://kernel.ozwillo.com
  client_secret: changeit

datacore:
  url: https://data.ozwillo.com
  systemAdminUser:
    refreshToken: changeit
    nonce: changeit

spring.data.mongodb.uri: mongodb://localhost/portal?connectTimeoutMS=300&journal=true
````

## Running the portal

* Run Spring Boot

```
./gradlew bootRun
```

* Run webpack-dev-server

```
yarn start
```

Open http://localhost:3000/

NB. to be used, Portal features require Kernel and Datacore servers to be deployed and configured in [application.yml](https://github.com/ozwillo/ozwillo-portal/blob/master/src/main/resources/application.yml).
You can deploy your own, or [ask on the ML](https://github.com/ozwillo/ozwillo-issues#other-information-channels) to get access to the online Ozwillo dev environment.
Also, a valid refresh_token needs to be set in the configuration (explained below).

## Using the portal

* Front-end access: 
  * Dev: http://localhost:3000
  * Prod: https://portal.ozwillo.com

## Renew Refresh_Token to DC http access

 * Use the js client app to generate a refresh_token that must be set in file application.yml (Portal and DC credentials can be found in **DC project/puppet** protected repository): https://github.com/ozwillo/ozwillo-node-client
 * With the obtained new code, reset it in the application.yml file and restart the Portal app

## Release

### Build the jar

```
./gradlew assemble
```

A new self-executable jar file, named `ozwillo-portal-${version}.jar` is generated in the `build/libs` directory.

=====================================================

Copyright (c) 2013-2019 Open Wide & Ozwillo - http://www.openwide.fr - https://www.ozwillo.com

=====================================================
