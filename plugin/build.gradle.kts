import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))

    compileOnly("me.croabeast:UpdateChecker:1.0")
    compileOnly("me.croabeast:VaultAdapter:1.2")
    compileOnly("me.croabeast:CommandFramework:1.2.1")
    compileOnly("me.croabeast:AdvancementInfo:1.0")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
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
