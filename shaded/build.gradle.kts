import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":core"))

    implementation("me.croabeast:YAML-API:1.1")
    implementation("me.croabeast:GlobalScheduler:1.1")
    implementation("me.croabeast:PrismaticAPI:1.1")
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
