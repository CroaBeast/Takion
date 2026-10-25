plugins {
    id("java-library")
    id("io.freefair.lombok") version "9.5.0"
    id("com.gradleup.shadow") version "9.4.1"
}

allprojects {
    group = "me.croabeast.takion"
    version = "2.2.3"

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
        // Adventure 5 declares Java 21; it is only compiled against (compileOnly), never shipped
        disableAutoTargetJvm()
    }

    // Adventure 5 is Java 21 class files, so every tool that reads it runs on JDK 21 when Gradle runs on less
    val jdk21: JavaToolchainSpec.() -> Unit = { languageVersion.set(JavaLanguageVersion.of(21)) }
    val needsJdk21 = !JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_21)

    tasks.withType<io.freefair.gradle.plugins.lombok.tasks.Delombok>().configureEach {
        if (needsJdk21) launcher.set(javaToolchains.launcherFor(jdk21))
    }

    tasks.withType<Javadoc>().configureEach {
        isFailOnError = false
        if (needsJdk21) javadocTool.set(javaToolchains.javadocToolFor(jdk21))

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

        if (needsJdk21) javaCompiler.set(javaToolchains.compilerFor(jdk21))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("snapshot.update", providers.systemProperty("snapshot.update").getOrElse("false"))
        if (needsJdk21) javaLauncher.set(javaToolchains.launcherFor(jdk21))
        testLogging {
            events("failed")
        }
    }

    val spigotVersion: String by project
    val annotationsVersion: String by project
    val lombokVersion: String by project
    val adventureVersion: String by project
    val bstatsVersion: String by project
    val yamlApiVersion: String by project
    val globalSchedulerVersion: String by project
    val prismaticVersion: String by project
    val vncVersion: String by project

    dependencies {
        compileOnly("org.spigotmc:spigot-api:$spigotVersion")

        compileOnly("org.jetbrains:annotations:$annotationsVersion")
        annotationProcessor("org.jetbrains:annotations:$annotationsVersion")

        compileOnly("org.projectlombok:lombok:$lombokVersion")
        annotationProcessor("org.projectlombok:lombok:$lombokVersion")

        compileOnly("me.clip:placeholderapi:2.12.2")

        compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
        compileOnly("net.kyori:adventure-text-logger-slf4j:$adventureVersion")

        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        compileOnly("net.luckperms:api:5.5")
        compileOnly("com.loohp:InteractiveChat:4.3.3.0") {
            isTransitive = false
        }

        compileOnly("org.bstats:bstats-bukkit:$bstatsVersion")

        compileOnly("me.croabeast:YAML-API:$yamlApiVersion")
        compileOnly("me.croabeast:GlobalScheduler:$globalSchedulerVersion")
        compileOnly("me.croabeast:PrismaticAPI:$prismaticVersion")

        // Used directly by common/reflect/Craft and common/util/ServerInfoUtils. It used to arrive
        // shaded inside the PrismaticAPI jar, which stops being true when Prismatic is built from source.
        // Transitive on purpose: the published jar is a fat jar with an empty pom, but when the
        // composite substitutes it for the local build the classes live in :core, :bukkit and friends.
        compileOnly("me.croabeast.vnc:VNC:$vncVersion")

        testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        testImplementation("org.spigotmc:spigot-api:$spigotVersion")
        testImplementation("me.croabeast:PrismaticAPI:$prismaticVersion")
        testImplementation("me.croabeast.vnc:VNC:$vncVersion")
        testImplementation("me.croabeast:GlobalScheduler:$globalSchedulerVersion")
        testImplementation("net.kyori:adventure-api:$adventureVersion")
        testImplementation("net.kyori:adventure-text-minimessage:$adventureVersion")
        testImplementation("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    }
}
