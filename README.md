OASIS portal implementation
===========================

## Prerequisites

Building requires **Java 8** and **Maven 3**.

"Unit" tests require a working **MongoDB** database at localhost:27017. We use 2.6 in the project; it may or may not work with older versions. "Integration" tests require an Internet connection and generally that the OASIS ecosystem (kernel, data core, etc.) is set up and in the expected condition. They are therefore more brittle and for that reason, they do not fail the build (you should make sure they pass, though).

Before building the project, you need to clone and install the [Spring integration package](https://github.com/pole-numerique/oasis-spring-integration) :

```
git clone git@github.com:pole-numerique/oasis-spring-integration.git
cd oasis-spring-integration
mvn clean install
```


## Building the sources

```
# NB. first compile pole-numerique/oasis-spring-integration.git
git clone git@github.com:pole-numerique/oasis-portal.git
# or if you already cloned it :
#git checkout master ; git pull
# make sure it has the **right version** (ex. 1.6 on 20150325), else look in [the root pom.xml](https://github.com/pole-numerique/oasis-portal/blob/master/portal-parent/pom.xml#L83) :
git checkout oasis-spring-integration-1.6
cd oasis-portal/portal-parent
mvn clean install
#or if you dont want to run the tests 
#mvn clean install -DskipTests
```

## Running the portal

The portal gets built in portal-parent/oasis-portal-front/target/oasis-portal-front-${VERSION}.jar.

Only once after install, change in the https://github.com/pole-numerique/oasis-portal/blob/master/portal-parent/oasis-portal-front/src/test/resources/application.yml the ```web.sitemap.refresh: 0 * * * * ?``` line in order to fill mongo with required header & footer cached data. Once it is filled (it can be seen in the logs, wait 2 minute, so that it will have been filled twice), change it back.

There are the two preferred ways to run the app:

* By running the apps the old-fashioned way (callin the main() method), either from your IDE or in command-line

```
cd oasis-portal-front/target
java -Xmx1G -jar oasis-portal-front-$VERSION.jar
# debug (-Djsse.enableSNIExtension=true by default):
#java -Xmx1G -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8003 -jar target/oasis-portal-front-${VERSION}.jar
```

* By running Spring Boot from the command line

```
cd oasis-portal-front
# debug :
#export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8003,server=y,suspend=n"
mvn spring-boot:run
```
which allows hotswapping (including of thymeleaf templates if cache set to false in [application.yml](https://github.com/pole-numerique/oasis-portal/blob/master/portal-parent/oasis-portal-front/src/main/resources/application.yml), default)

And finally open http://localhost:8080/en/store (root would redirect to the Joomla server) with your favorite browser.

NB. to be used, Portal features require Kernel and Datacore servers to be deployed and configured in [application.yml](https://github.com/pole-numerique/oasis-portal/blob/master/portal-parent/oasis-portal-front/src/main/resources/application.yml).
You can deploy your own, or [ask](http://www.ozwillo.com) to get access to the online Ozwillo dev environment.
Also, a valid refresh_token needs to be set in the configuration (explained below).

## Debugging js :
in [application.yml](https://github.com/pole-numerique/oasis-portal/blob/master/portal-parent/oasis-portal-front/src/main/resources/application.yml), set spring: thymeleaf: cache: to false

## Configuring the reverse-proxy

The front-end is run on port 8080, but the config files point to "localhost". It is therefore recommended to configure a reverse proxy on your machine. Here is an example Apache site:

**/etc/apache2/sites-available/oasis-portal.conf**

```
<VirtualHost *:80>
	ServerName www.ozwillo.local

	ProxyPreserveHost On
	
	ProxyPass / http://localhost:8080/ retry=0
	ProxyPassReverse / http://localhost:8080/

	ErrorLog ${APACHE_LOG_DIR}/error.log
	CustomLog ${APACHE_LOG_DIR}/access.log combined
</VirtualHost>
```

```
sudo a2enmod proxy_http
sudo a2ensite oasis-portal.conf
sudo service apache2 restart
```


## Using the portal

* Front-end access: 
  * DEV  : http://localhost:8080/my
  * PROD : http://www.ozwillo.com

## Using Citizen-Kin (app in Portal)

* Citizen accounts:
  * alice@example.com / alice
  * bob@example.net / bob
* Agents accounts:
  * john.doe@example.net / john.doe@example.net
  * robert.roe@example.net / robert.roe@example.net
  * carla.coe@example.net / carla.coe@example.net

## Renew Refresh_Token to DC http access

 * Use the js client app to generate a refresh_token that must be set in file application.yml (Portal and DC credentials can be found in **DC project/puppet** protected repertory) :
    https://github.com/ozwillo/ozwillo-node-client
 * With the obtained new code, reset it in the application.yml file and restart the Portal app

## Release

### Compile thymeleaf templates & minify :
```
# manage several version of node using https://github.com/creationix/nvm
# see also http://www.mattpalmerlee.com/2013/03/23/installing-and-switching-between-multiple-versions-of-node-js-n-vs-nvm/
wget -qO- https://raw.githubusercontent.com/creationix/nvm/master/install.sh | bash
source ~/.bashrc
nvm install v0.10.36

# install react-tools :
npm install -g uglify-js
#npm install jsx
npm install -g react-tools
#react-tools@0.12.2 /home/mdutoo/.nvm/v0.10.36/lib/node_modules/react-tools

# compile & minify :
Install python (2.7) using your favorite package manager, then :
./jsx.py
# see also http://facebook.github.io/react/docs/getting-started.html
```

### Git Tagging, JAR Generation and Installation of sources

For the release, a new tag must be created as follows `git tag $TAG_RELEASE_NAME_VERSION`.

Then in the server, the code and TAG must be pulled and there set the TAG in git and generate the JAR with `mvn clean install`. 

The generated JAR file must be placed in the running server location (apache/tomcat) or in the configured directory for spring-boot / java -jar.
