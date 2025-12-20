import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":core"))

    implementation("me.croabeast:YAML-API:1.1")
    implementation("me.croabeast:GlobalScheduler:1.1")
    implementation("me.croabeast:PrismaticAPI:1.1")
    implementation("me.croabeast:UpdateChecker:1.0")
    implementation("me.croabeast:VaultAdapter:1.1")
    implementation("me.croabeast:CommandFramework:1.1")
    implementation("me.croabeast:AdvancementInfo:1.0")
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
}
