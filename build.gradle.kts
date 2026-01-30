plugins {
    kotlin("jvm") version "2.3.0-Beta1"
    id("java-library")
    id("io.freefair.lombok") version "8.10"
    id("com.gradleup.shadow") version "8.3.0"
}

allprojects {
    group = "me.croabeast.takion"
    version = "1.4"

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://croabeast.github.io/repo/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.loohpjames.com/repository")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "io.freefair.lombok")

    java {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false

        (options as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:none", "-quiet")
            encoding = "UTF-8"
            charSet = "UTF-8"
            docEncoding = "UTF-8"

            if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_1_9))
                addBooleanOption("html5", true)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        options.compilerArgs.add("-Xlint:-options")
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

        compileOnly("org.jetbrains:annotations:26.0.2-1")
        annotationProcessor("org.jetbrains:annotations:26.0.2-1")

        compileOnly("org.projectlombok:lombok:1.18.42")
        annotationProcessor("org.projectlombok:lombok:1.18.42")

        compileOnly("me.clip:placeholderapi:2.11.6")

        compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
        compileOnly("net.kyori:adventure-text-serializer-legacy:4.25.0")
        compileOnly("net.kyori:adventure-text-logger-slf4j:4.25.0")

        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        compileOnly("net.luckperms:api:5.5")
        compileOnly("com.loohp:InteractiveChat:4.3.3.0")
        compileOnly("org.bstats:bstats-bukkit:3.0.2")
        compileOnly("com.github.stefvanschie.inventoryframework:IF:0.11.6")
        compileOnly("com.mojang:authlib:1.5.25")

        compileOnly("me.croabeast:YAML-API:1.1")
        compileOnly("me.croabeast:GlobalScheduler:1.1")
        compileOnly("me.croabeast:PrismaticAPI:1.1")
    }
}
