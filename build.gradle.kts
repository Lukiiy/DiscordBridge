plugins { java }

group = "me.lukiiy"
description = project.property("description")?.toString()

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    if (project.name != "common") dependencies { implementation(project(":common")) }
}

tasks {
    jar { enabled = false }
}