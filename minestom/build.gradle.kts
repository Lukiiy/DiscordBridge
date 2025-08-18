plugins {
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "me.lukiiy"
description = rootProject.property("description").toString()
version = "1.0-Minestom"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2025.07.11-1.21.7")
    implementation(project(":DSerialAdvnt"))
    implementation("org.slf4j:slf4j-nop:2.0.17")
}

val displayName = rootProject.property("display").toString()

tasks {
    shadowJar {
        archiveBaseName.set(displayName)
        archiveClassifier.set("")
        mergeServiceFiles()
        // minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
        manifest { attributes["Main-Class"] = "me.lukiiy.discordBridge.Main" }
    }
}

val jvm = 21

java.toolchain { languageVersion.set(JavaLanguageVersion.of(jvm)) }
kotlin { jvmToolchain(jvm) }