val jenaVersion = "4.1.0"
val springVersion = "6.1.9"
val springBootVersion = "2.7.18"
val jakartaAnnotationApiVersion = "3.0.0"
val guavaVersion = "30.0-jre"


plugins {
    java
}

group = "zone.cogni.libs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.jena:jena-arq:$jenaVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.springframework:spring-expression:$springVersion")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")

    testImplementation("org.springframework:spring-core:$springVersion")
    testImplementation("org.springframework:spring-context:$springVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}

tasks.test {
    useJUnitPlatform()
}