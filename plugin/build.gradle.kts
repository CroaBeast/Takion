import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":core"))

    compileOnly("me.croabeast:UpdateChecker:1.0")
    compileOnly("me.croabeast:VaultAdapter:1.1")
    compileOnly("me.croabeast:CommandFramework:1.1")
    compileOnly("me.croabeast:AdvancementInfo:1.0")

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
