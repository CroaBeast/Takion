plugins {
    kotlin("jvm") version "2.4.0-Beta2"
    id("java-library")
    id("io.freefair.lombok") version "9.5.0"
    id("com.gradleup.shadow") version "9.4.1"
}

allprojects {
    group = "me.croabeast.takion"
    version = "2.0.2"

    repositories {
        mavenCentral()

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

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("snapshot.update", providers.systemProperty("snapshot.update").getOrElse("false"))
        testLogging {
            events("failed")
        }
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
        compileOnly("me.croabeast:PrismaticAPI:2.0.1")

        // Used directly by common/reflect/Craft and common/util/ServerInfoUtils. It used to arrive
        // shaded inside the PrismaticAPI jar, which stops being true when Prismatic is built from source.
        // Transitive on purpose: the published jar is a fat jar with an empty pom, but when the
        // composite substitutes it for the local build the classes live in :core, :bukkit and friends.
        compileOnly("me.croabeast.vnc:VNC:1.3.1")

        testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        testImplementation("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
        testImplementation("me.croabeast:PrismaticAPI:2.0.1")
        testImplementation("me.croabeast.vnc:VNC:1.2.1")
        testImplementation("me.croabeast:GlobalScheduler:1.1")
        testImplementation("net.kyori:adventure-api:4.26.1")
        testImplementation("net.kyori:adventure-text-minimessage:4.26.1")
        testImplementation("net.kyori:adventure-text-serializer-legacy:4.26.1")
    }
}
