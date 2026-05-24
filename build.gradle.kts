plugins {
    kotlin("jvm") version "2.4.0-Beta2"
    id("java-library")
    id("io.freefair.lombok") version "9.5.0"
    id("com.gradleup.shadow") version "9.4.1"
}

allprojects {
    group = "me.croabeast.takion"
    version = "1.6.2"

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

        compileOnly("org.jetbrains:annotations:26.1.0")
        annotationProcessor("org.jetbrains:annotations:26.1.0")

        compileOnly("org.projectlombok:lombok:1.18.46")
        annotationProcessor("org.projectlombok:lombok:1.18.46")

        compileOnly("me.clip:placeholderapi:2.12.2")

        compileOnly("net.kyori:adventure-text-minimessage:4.26.1")
        compileOnly("net.kyori:adventure-text-serializer-legacy:4.26.1")
        compileOnly("net.kyori:adventure-text-logger-slf4j:4.26.1")

        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        compileOnly("net.luckperms:api:5.5")
        compileOnly("com.loohp:InteractiveChat:4.3.3.0") {
            isTransitive = false
        }

        compileOnly("org.bstats:bstats-bukkit:3.2.1")
        compileOnly("com.github.stefvanschie.inventoryframework:IF:0.12.0")
        
        compileOnly("me.croabeast:YAML-API:1.1")
        compileOnly("me.croabeast:GlobalScheduler:1.1")
        compileOnly("me.croabeast:PrismaticAPI:1.5.0")
    }
}
