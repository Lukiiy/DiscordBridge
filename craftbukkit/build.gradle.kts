plugins { id("com.gradleup.shadow") version "8.3.0" }

group = rootProject.group
version = "1.6-CB"

dependencies {
    compileOnly(files("lib/craftbukkit-1060.jar")) // Please use your own copy!
    implementation(project(":DSerial"))
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