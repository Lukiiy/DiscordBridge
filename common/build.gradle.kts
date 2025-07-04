plugins { kotlin("jvm") version "2.2.0" }

group = rootProject.group

dependencies {
    api("net.dv8tion:JDA:5.6.1") {
        exclude(module = "opus-java")
    }
    api(kotlin("stdlib-jdk8"))
}

kotlin { jvmToolchain(8) }