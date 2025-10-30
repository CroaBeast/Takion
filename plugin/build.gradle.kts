import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(project(":core"))

    implementation("me.croabeast:VaultAdapter:1.1")
    implementation("me.croabeast:YAML-API:1.1")
    implementation("me.croabeast:GlobalScheduler:1.0")
    implementation("me.croabeast:PrismaticAPI:1.1")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    exclude(
        "META-INF/**", "org/apache/commons/**", "org/intellij/**", "org/jetbrains/**",
        "me/croabeast/file/plugin/YAMLPlugin.*", "plugin.yml"
    )
    relocate("org.bstats", "me.croabeast.metrics")
}
