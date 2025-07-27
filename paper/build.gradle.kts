plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = rootProject.group
version = "1.5-Paper"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    implementation(project(":DSerialAdvnt")) {
        exclude(group = "net.kyori", module = "adventure-text-minimessage")
        exclude(group = "net.kyori", module = "adventure-text-serializer-legacy")
        exclude(group = "net.kyori", module = "adventure-text-serializer-plain")
    }
    compileOnly("me.clip:placeholderapi:2.11.6")
}

val displayName = rootProject.property("display").toString()

tasks {
    shadowJar {
        archiveBaseName.set(displayName)
        archiveClassifier.set("")
        mergeServiceFiles()
        minimize()
    }

    jar { enabled = false }

    build { dependsOn(shadowJar) }

    processResources {
        val props = mapOf(
            "version" to version,
            "name" to displayName,
            "desc" to rootProject.property("description").toString(),
            "web" to rootProject.property("web").toString()
        )

        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") { expand(props) }
    }
}

kotlin { jvmToolchain(21) }