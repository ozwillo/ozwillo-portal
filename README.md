OASIS portal implementation
===========================

BUILDING
--------

TL;DR:
   cd portal-parent
   mvn install
   java -jar oasis-portal-mockserver/target/oasis-portal-mockserver-1.0-SNAPSHOT.jar
   mvn clean compile test integration-test failsafe:verify install

Building requires Java 8 and Maven 3. Provided you have both, it should be pretty easy to use portal-parent/pom.xml to build the application.

Note: "unit" tests require a working MongoDB database at localhost:27017. We use 2.6 in the project; it may or may not work with older versions. "Integration" tests require an Internet connection as well as a running mockserver, which is a submodule of the project. Integration tests do not fail the build, so that's not a paradox.

RUNNING
-------

All applications use Spring Boot. They produce a runnable Jar that embeds Tomcat. As a special case, the Front office is generated as a War that, while still runnable with java -jar oasis-portal-front-${VERSION}.war, is deployable in non-embedded Tomcat. This is so we can use some of Tomcat's advanced configuration options in production (namely, session replication / clustering).
