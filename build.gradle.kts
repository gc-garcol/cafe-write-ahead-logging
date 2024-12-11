import org.jreleaser.model.Active

plugins {
    `java-library`
    `maven-publish`
    id("org.jreleaser") version "1.15.0"
}

group = "io.github.gc-garcol"
version = "0.3.0"

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
                    url.set("https://github.com/gc-garcol/cafe-write-ahead-logging/tree/main")
                }
            }
        }
    }
}

jreleaser {
    signing {
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = Active.ALWAYS
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add("wal-core/build/staging-deploy")
                }
            }
        }
    }
}