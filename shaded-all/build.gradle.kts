import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":core"))

    implementation("com.mojang:authlib:1.5.25")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.11.6")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("me.croabeast:YAML-API:1.1")
    implementation("me.croabeast:GlobalScheduler:1.1")
    implementation("me.croabeast:PrismaticAPI:1.1")
    implementation("me.croabeast:UpdateChecker:1.0")
    implementation("me.croabeast:VaultAdapter:1.1")
    implementation("me.croabeast:CommandFramework:1.2")
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
