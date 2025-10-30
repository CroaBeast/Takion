plugins {
    id("java-library")
    id("io.freefair.lombok")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(8)) }
    withSourcesJar()
    withJavadocJar()
}

tasks.named("build") {}
