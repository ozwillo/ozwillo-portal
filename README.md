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
git clone git@github.com:pole-numerique/oasis-portal.git
cd oasis-portal/portal-parent
mvn clean install
```

## Running the portal

The portal gets built in portal-parent/oasis-portal-front/target/oasis-portal-front-${VERSION}.jar.

There are the two preferred ways to run the app:

* By running the apps the old-fashioned way (callin the main() method), either from your IDE or in command-line

```
cd oasis-portal-front/target
java -jar oasis-portal-front-${VERSION}.jar
```

* By running Spring Boot from the command line

```
cd oasis-portal-front
mvn spring-boot:run
```

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

* Front-end access: http://www.ozwillo.local

* Citizen accounts:
  * alice@example.com / alice
  * bob@example.net / bob
* Agents accounts:
  * john.doe@example.net / john.doe@example.net
  * robert.roe@example.net / robert.roe@example.net
  * carla.coe@example.net / carla.coe@example.net

