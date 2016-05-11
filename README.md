=====================================================

[Ozwillo](https://www.ozwillo.com) - Portal

Copyright (c) 2013-2016 Open Wide & Ozwillo - http://www.openwide.fr - https://www.ozwillo.com

=====================================================

Ozwillo portal implementation
===========================

## Prerequisites

Building requires **Java 8**.

"Unit" tests require a working **MongoDB** database at localhost:27017. We use 2.6 in the project; it may or may not work with older versions. "Integration" tests require an Internet connection and generally that the Ozwillo ecosystem (kernel, data core, etc.) is set up and in the expected condition. They are therefore more brittle and for that reason, they do not fail the build (you should make sure they pass, though).


## Installation

* Before building the project, you need to clone and install the [Spring integration package](https://github.com/ozwillo/ozwillo-java-spring-integration) :

```
git clone git@github.com:ozwillo/ozwillo-java-spring-integration.git
cd ozwillo-java-spring-integration
./gradlew install
```

* After cloning this repository, install the npm packages

```
nvm install 5.6.0 (or nvm use 5.6.0 if you already have Node 5.6.0 installed)
npm install
```

## Running the portal

* Run Spring Boot

```
./gradlew bootRun
```

* Run webpack-dev-server

```
npm run start
```

Open http://localhost:8080/my (root would redirect to the [Ozwillo website](https://www.ozwillo.com)) with your favorite browser.

NB. to be used, Portal features require Kernel and Datacore servers to be deployed and configured in [application.yml](https://github.com/ozwillo/ozwillo-portal/blob/master/src/main/resources/application.yml).
You can deploy your own, or [ask on the ML](https://github.com/ozwillo/ozwillo-issues#other-information-channels) to get access to the online Ozwillo dev environment.
Also, a valid refresh_token needs to be set in the configuration (explained below).

## Using the portal

* Front-end access: 
  * DEV  : http://localhost:8080/my
  * PROD : http://www.ozwillo.com

## Renew Refresh_Token to DC http access

 * Use the js client app to generate a refresh_token that must be set in file application.yml (Portal and DC credentials can be found in **DC project/puppet** protected repository) :
    https://github.com/ozwillo/ozwillo-node-client
 * With the obtained new code, reset it in the application.yml file and restart the Portal app

## Release

### Build the jar

```
./gradlew assemble
```
