val jenaVersion = "4.10.0"
val guavaVersion = "33.3.0-jre"
val springVersion = "5.3.+"
val jakartaAnnotationApiVersion = "3.0.0"
val jb4jsonldJacksonVersion = "0.14.3"
val logbackVersion = "1.5.7"
val jupiterVersion = "5.11.0"

plugins {
    `java-library`
    pmd
    jacoco
    id("io.freefair.lombok") version "8.10"
    id("org.owasp.dependencycheck") version "10.0.3"
    id("maven-publish")
    id("signing")
    id("pl.allegro.tech.build.axion-release") version "1.13.3"
}

group = "zone.cogni.semanticz"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withJavadocJar()
    withSourcesJar()
}

scmVersion {
    tag.apply {
        prefix = "v"
        versionSeparator = ""
        branchPrefix = mapOf(
            "release/.*" to "release-v",
            "hotfix/.*" to "hotfix-v"
        )
    }
    nextVersion.apply {
        suffix = "SNAPSHOT"
        separator = "-"
    }
}

pmd {
    isIgnoreFailures = true
    isConsoleOutput = true
    toolVersion = "7.0.0"
    rulesMinimumPriority = 5
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
    implementation("cz.cvut.kbss.jsonld:jb4jsonld-jackson:$jb4jsonldJacksonVersion")

    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register("qualityCheck") {
    dependsOn(tasks.pmdMain)
    dependsOn(tasks.pmdTest)
    dependsOn(tasks.jacocoTestReport)
    dependsOn(tasks.dependencyCheckAnalyze)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.jar {
    from("${projectDir}") {
        include("LICENSE")
        into("/")
    }
    from("${projectDir}") {
        include("LICENSE")
        into("META-INF")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Semanticz")
                description.set("This project serves for generating IRIs using a predefined template based on existing RDF data.")
                url.set("https://github.com/cognizone/semanticz")

                scm {
                    connection.set("scm:git@github.com:cognizone/semanticz-irigenerator.git")
                    developerConnection.set("scm:git@github.com:cognizone/semanticz-irigenerator.git")
                    url.set("https://github.com/cognizone/semanticz-irigenerator")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("cognizone")
                        name.set("Cognizone")
                        email.set("dev@cognizone.com")
                    }
                }
            }
        }
    }

    repositories {
        if (project.hasProperty("publishToMavenCentral")) {
            maven {
                credentials {
                    username = System.getProperty("ossrh.username")
                    password = System.getProperty("ossrh.password")
                }
                val stagingRepoUrl = "${System.getProperty("ossrh.url")}/service/local/staging/deploy/maven2"
                val snapshotsRepoUrl = "${System.getProperty("ossrh.url")}/content/repositories/snapshots"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else stagingRepoUrl)
            }
        }
    }
}

tasks.withType<Javadoc> {
    options {
        (this as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
    }
    isFailOnError = false
}

signing {
    useInMemoryPgpKeys(
        project.findProperty("signing.keyId")?.toString(),
        project.findProperty("signing.password")?.toString(),
        project.findProperty("signing.secretKeyRingFile")?.toString()
    )
    if (project.hasProperty("publishToMavenCentral")) {
        sign(publishing.publications["mavenJava"])
    }
}

tasks.withType<PublishToMavenRepository> {
    dependsOn(tasks.withType<Sign>())
}
