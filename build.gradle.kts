import com.moowork.gradle.node.yarn.YarnTask

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    java
    idea
    id("org.springframework.boot") version "2.1.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    id("com.github.node-gradle.node") version "1.3.0"
}

version = "1.54.0-RC2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

node {
    version = "10.14.1"
    yarnVersion = "1.12.3"
    download = true
}

repositories {
    mavenCentral()
    // uncomment this when locally developing on ozwillo-java-spring-integration lib
    // mavenLocal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    /* Spring Boot managed dependencies
         do not specify a version to let them live along with Spring Boot without extra hassle */
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.hazelcast:hazelcast")
    implementation("com.hazelcast:hazelcast-spring")
    implementation("joda-time:joda-time")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.httpcomponents:httpcore")
    implementation("org.apache.commons:commons-csv:1.5")
    implementation("com.github.ozwillo:ozwillo-java-spring-integration:1.27.0")
    // uncomment this when locally developing on ozwillo-java-spring-integration lib
    // implementation("com.ozwillo:ozwillo-java-spring-integration:1.27.0-RC2")

    implementation("io.projectreactor:reactor-core")

    /* Our specific dependencies */
    implementation("de.javakaffee.msm:memcached-session-manager:2.1.1")
    implementation("de.javakaffee.msm:memcached-session-manager-tc8:2.1.1")
    implementation("org.apache.tika:tika-core:1.16")

    implementation("org.commonjava.googlecode.markdown4j:markdown4j:2.2-cj-1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    runtime("commons-fileupload:commons-fileupload:1.3.3")

    // required to parse logback-spring.groovy
    // TODO : quite a fat dependency for a configuration file
    runtime("org.codehaus.groovy:groovy:2.4.7")

    /* Runtime dependencies brought by ozwillo-java-spring-integration */
    runtime("com.google.code.gson:gson:2.8.2")
    runtime("com.ibm.icu:icu4j:60.1")
}

defaultTasks("bootRun")

tasks.bootRun {
    environment("SPRING_PROFILES_ACTIVE", "dev")
}

tasks.bootJar {
    launchScript {
        properties(mapOf("initInfoProvides" to "ozwillo-portal"))
    }
}

tasks.getting(ProcessResources::class) {
    exclude("public/js", "public/css")
}

tasks.register<YarnTask>("frontBundle") {
    args = listOf("run", "build")
    doLast {
        copy {
            from("src/main/resources/public/build")
            into("build/resources/main/public/build")
        }
    }
}

tasks.named("jar") {
    dependsOn("frontBundle")
}

tasks.named("frontBundle") {
    dependsOn("yarn_install")
}

