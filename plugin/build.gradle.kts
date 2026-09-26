import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val updateCheckerVersion: String by project
val vaultAdapterVersion: String by project
val commandFrameworkVersion: String by project
val advancementInfoVersion: String by project

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))

    compileOnly("me.croabeast:UpdateChecker:$updateCheckerVersion")
    compileOnly("me.croabeast:VaultAdapter:$vaultAdapterVersion")
    compileOnly("me.croabeast:CommandFramework:$commandFrameworkVersion")
    compileOnly("me.croabeast:AdvancementInfo:$advancementInfoVersion")
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
        "META-INF/**", "org/apache/commons/**", "org/intellij/**", "org/jetbrains/**"
    )
    relocate("org.bstats", "me.croabeast.metrics")
}
