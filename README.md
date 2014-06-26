OASIS portal implementation
===========================

## Prerequisites

Building requires **Java 8** and **Maven 3**.

"Unit" tests require a working **MongoDB** database at localhost:27017. We use 2.6 in the project; it may or may not work with older versions. "Integration" tests require an Internet connection as well as a running **mockserver** (see below), which is a submodule of the project. Integration tests do not fail the build, so that's not a paradox.

Before building the project, you need to clone and install the [Spring integration package](https://github.com/pole-numerique/oasis-spring-integration) :

```
git clone git@github.com:pole-numerique/oasis-spring-integration.git
cd oasis-spring-integration
mvn clean install
```


## Building the sources

```
git clone git@github.com:pole-numerique/oasis-portal.git
cd oasis-portal/portal-parent
mvn clean install
```

To be able to launch the integration tests (and then run the application), you have at the moment to launch a mock server:

```
java -jar oasis-portal-mockserver/target/oasis-portal-mockserver-1.0-SNAPSHOT.jar
```

Then in another console:

```
mvn clean test integration-test failsafe:verify
```


## Running the portal

The portal comes in two independent parts: the back-end (oasis-portal-back) and the front-end (oasis-portal-front). Both can be run the same way, and both require a mock server to be launched at the moment:

`java -jar oasis-portal-mockserver/target/oasis-portal-mockserver-1.0-SNAPSHOT.jar`

There are the two preferred ways to run the apps:

* By running the apps the old-fashioned way (callin the main() method), either from your IDE or in command-line

```
cd oasis-portal-front/target
java -jar oasis-portal-front-1.0-SNAPSHOT.war
```

* By running Spring Boot from the command line

```
cd oasis-portal-front
mvn spring-boot:run
```

> Note: All applications use Spring Boot. They produce a runnable Jar that embeds Tomcat. As a special case, the Front office is generated as a War that, while still runnable with java -jar oasis-portal-front-${VERSION}.war, is deployable in non-embedded Tomcat. This is so we can use some of Tomcat's advanced configuration options in production (namely, session replication / clustering).

## Configuring the reverse-proxy

The front-end is run on port 8080, the back-end on port 8082. To prevent cookie-related issues, it is recommended to configure a reverse proxy on your machine. Here is an example Apache site:

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

<VirtualHost *:80>
	ServerName admin.ozwillo.local
	
	ProxyPreserveHost On

	ProxyPass / http://localhost:8082/ retry=0
	ProxyPassReverse / http://localhost:8082/

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

* Front-end access: http://www.ozwillo.local
* Back-end access: http://admin.ozwillo.local

* Citizen accounts:
  * alice@example.com / alice
  * bob@example.net / bob
* Agents accounts:
  * john.doe@example.net / john.doe@example.net
  * robert.roe@example.net / robert.roe@example.net
  * carla.coe@example.net / carla.coe@example.net

