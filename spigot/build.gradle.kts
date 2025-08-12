plugins { id("com.gradleup.shadow") version "8.3.0" }

group = rootProject.group
version = "1.6-Spigot"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:4.4.0")
    implementation(project(":DSerialAdvnt"))
    compileOnly("me.clip:placeholderapi:2.11.6")
}

val displayName = rootProject.property("display").toString()

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

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
        filesMatching("plugin.yml") { expand(props) }
    }
}