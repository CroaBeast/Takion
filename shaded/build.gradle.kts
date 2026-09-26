import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

plugins {
    `maven-publish`
}

val allBundle by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val yamlApiVersion: String by project
val globalSchedulerVersion: String by project
val prismaticVersion: String by project
val inventoryFrameworkVersion: String by project
val bstatsVersion: String by project
val updateCheckerVersion: String by project
val vaultAdapterVersion: String by project
val commandFrameworkVersion: String by project
val advancementInfoVersion: String by project

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))

    implementation("me.croabeast:YAML-API:$yamlApiVersion")
    implementation("me.croabeast:GlobalScheduler:$globalSchedulerVersion")
    implementation("me.croabeast:PrismaticAPI:$prismaticVersion")

    allBundle("com.github.stefvanschie.inventoryframework:IF:$inventoryFrameworkVersion")
    allBundle("org.bstats:bstats-bukkit:$bstatsVersion")
    allBundle("me.croabeast:UpdateChecker:$updateCheckerVersion")
    allBundle("me.croabeast:VaultAdapter:$vaultAdapterVersion")
    allBundle("me.croabeast:CommandFramework:$commandFrameworkVersion")
    allBundle("me.croabeast:AdvancementInfo:$advancementInfoVersion")
}

fun ShadowJar.configureBaseShadow() {
    exclude(
        "META-INF/**", "org/apache/commons/**", "org/intellij/**", "org/jetbrains/**",
        "me/croabeast/file/plugin/YAMLPlugin.*", "plugin.yml"
    )
}

fun ShadowJar.configureAllShadow() {
    exclude(
        "META-INF/**", "org/apache/commons/**", "org/intellij/**", "org/jetbrains/**",
        "com/google/**", "javax/**", "org/apache/logging/**", "**/**.xsd", "**/**.dtd",
        "fonts/**", "**/**.der", "me/croabeast/*/plugin/**", "plugin.yml"
    )
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
    dependsOn(tasks.named("allShadowJar"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    configureBaseShadow()
}

val allShadowJar = tasks.register<ShadowJar>("allShadowJar") {
    archiveClassifier.set("all")
    from(sourceSets.main.get().output)
    configurations = listOf(project.configurations.runtimeClasspath.get(), allBundle)
    configureAllShadow()
}

// Declares the all jar as an outgoing artifact so composite builds that substitute
// me.croabeast.takion:shaded:<version>:all for this project (CyberCore does) can resolve the
// classifier. Without it the substitution fails with "Could not find shaded-all.jar".
artifacts {
    add("apiElements", allShadowJar)
    add("runtimeElements", allShadowJar)
}

publishing {
    publications {
        create<MavenPublication>("shaded") {
            artifactId = "shaded"
            artifact(tasks.named<ShadowJar>("shadowJar"))
            artifact(allShadowJar)
            artifact(tasks.named<Jar>("sourcesJar"))
            artifact(tasks.named<Jar>("javadocJar"))
        }
    }
}
