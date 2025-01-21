plugins {
    `java-library`
    pmd
    jacoco
    alias(libs.plugins.lombok)
    alias(libs.plugins.dependencycheck)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.axion.release)
}

group = "zone.cogni.semanticz"

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

version = scmVersion.version
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
    versionIncrementer("incrementPatch") // Increment the patch version
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
    implementation(libs.jena.arq)
    implementation(libs.guava)
    implementation(libs.spring.expression)
    implementation(libs.jakarta.annotation.api)
    implementation(libs.jb4jsonld.jackson)

    testImplementation(libs.logback.classic)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
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
        into("META-INF")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("semanticz-irigenerator")
                description.set("This project serves for generating IRIs using a predefined template based on existing RDF data.")
                url.set("https://github.com/cognizone/semanticz-irigenerator")

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
    if (project.hasProperty("publishToMavenCentral")) {
        sign(publishing.publications["mavenJava"])
    }
}
