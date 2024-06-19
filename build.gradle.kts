val jenaVersion = "4.10.0"
val springVersion = "5.3.+"
val jakartaAnnotationApiVersion = "3.0.0"
val guavaVersion = "33.2.1-jre"
val jupiterVersion = "5.10.2"

plugins {
    `java-library`
}

group = "zone.cogni.libs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11)) // specify the Java version
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

dependencies {
    implementation("org.apache.jena:jena-arq:$jenaVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.springframework:spring-expression:$springVersion")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
}

tasks.test {
    useJUnitPlatform()
}