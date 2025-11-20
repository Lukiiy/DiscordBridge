plugins {
    kotlin("jvm") version "2.2.0"
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
}

group = rootProject.group
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    val minecraftVersion = project.property("minecraft_version") as String
    val loaderVersion = project.property("loader_version") as String

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    implementation(files("lib/UniStyle-1.0-SNAPSHOT.jar"))
}

val displayName = rootProject.property("display").toString()

tasks {
    processResources {
        val props = mapOf(
            "version" to version,
            "minecraft_version" to project.property("minecraft_version").toString(),
            "loader_version" to project.property("loader_version").toString(),
            "name" to displayName,
            "desc" to rootProject.property("description").toString(),
            "web" to rootProject.property("web").toString()
        )

        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }

    build {
        dependsOn("remapJar") // fabric-loom task that remaps names
    }
}

kotlin.jvmToolchain(21)

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}