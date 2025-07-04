plugins { kotlin("jvm") version "2.2.0" }

group = rootProject.group

dependencies {
    api("net.kyori:adventure-text-minimessage:4.22.0")
    api("net.kyori:adventure-text-serializer-legacy:4.22.0")
    api("net.kyori:adventure-text-serializer-plain:4.22.0")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin { jvmToolchain(8) }