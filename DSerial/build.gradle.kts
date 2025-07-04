plugins { kotlin("jvm") version "2.2.0" }

group = rootProject.group

dependencies { api(kotlin("stdlib-jdk8")) }

kotlin { jvmToolchain(8) }