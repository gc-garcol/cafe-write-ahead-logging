plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.gc-garcol"
version = "0.0.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}


java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.gc-garcol"
            artifactId = "cafe-wal"

            from(components["java"])

            pom {
                name.set("cafe-wal")
                description.set("cafe-wal")
                url.set("https://github.com/gc-garcol/cafe-write-ahead-logging/tree/main")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://spdx.org/licenses/Apache-2.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("gc-garcol")
                        name.set("cafe")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/gc-garcol/cafe-write-ahead-logging.git")
                    developerConnection.set("scm:git:ssh://github.com/gc-garcol/cafe-write-ahead-logging.git")
                    url.set("https://github.com/gc-garcol/cafe-write-ahead-logging")
                }
            }
        }
    }

    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}