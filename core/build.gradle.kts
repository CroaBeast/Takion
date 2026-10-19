repositories {
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    api(project(":common"))

    // Optional: toasts are sent through PacketEvents only when its plugin is installed.
    compileOnly("com.github.retrooper:packetevents-api:2.14.0") { isTransitive = false }
    compileOnly("com.github.retrooper:packetevents-spigot:2.14.0") { isTransitive = false }
}

tasks.named("build") {}
