plugins {
    java
}

group = "gc.garcol"
version = "0.0.1-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val jmhVersion = "1.37"
var agronaVersion = "1.23.1"

dependencies {
    implementation(project(":wal-core"))
    // JMH
    implementation("org.openjdk.jmh:jmh-core:${jmhVersion}")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:${jmhVersion}")
}

tasks.withType<JavaCompile> {
    options.annotationProcessorPath = configurations.getByName("annotationProcessor")
}
