val asquareVersion = "0.7.0"
val springBootVersion = "2.7.18"

plugins {
    java
}

group = "zone.cogni.libs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("zone.cogni.asquare:triplestore-jena-memory:$asquareVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}

tasks.test {
    useJUnitPlatform()
}