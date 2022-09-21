buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    java
    idea
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
    id("com.github.node-gradle.node") version "3.2.1"
}

version = "1.56.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

node {
    version.set("16.14.0")
    yarnVersion.set("1.22.18")
    download.set(true)
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.hazelcast:hazelcast")
    implementation("com.hazelcast:hazelcast-spring")
    implementation("joda-time:joda-time")
    implementation("org.apache.httpcomponents:httpclient")
    implementation("org.apache.httpcomponents:httpcore")
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("com.github.ozwillo:ozwillo-java-spring-integration:1.28.0")
    // uncomment this when locally developing on ozwillo-java-spring-integration lib
    // implementation("com.ozwillo:ozwillo-java-spring-integration:1.27.0-RC2")
    implementation("io.projectreactor:reactor-core")

    /* Our specific dependencies */
    implementation("de.javakaffee.msm:memcached-session-manager:2.3.2")
    implementation("de.javakaffee.msm:memcached-session-manager-tc8:2.3.2")
    implementation("org.apache.tika:tika-core:2.4.1")

    implementation("org.commonjava.googlecode.markdown4j:markdown4j:2.2-cj-1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    runtimeOnly("commons-fileupload:commons-fileupload:1.4")

    /* Runtime dependencies brought by ozwillo-java-spring-integration */
    runtimeOnly("com.google.code.gson:gson:2.9.0")
    runtimeOnly("com.ibm.icu:icu4j:71.1")
}

defaultTasks("bootRun")

tasks.test {
    environment("SPRING_PROFILES_ACTIVE", "test")
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

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

tasks.register<com.github.gradle.node.yarn.task.YarnTask>("frontBundle") {
    args.set(listOf("run", "build"))
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
