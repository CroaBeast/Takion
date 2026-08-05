rootProject.name = "Takion"

// Builds PrismaticAPI from source and substitutes it for me.croabeast:PrismaticAPI, so the
// reworked Element API can be developed against before it is published. Locally the checkout is
// a sibling folder; publish.yml checks it out inside this repository instead. When neither path
// exists the build falls back to the published artifact.
listOf("../PrismaticAPI", "PrismaticAPI")
    .map(::file)
    .firstOrNull { it.resolve("settings.gradle.kts").isFile }
    ?.let(::includeBuild)

include("common", "core", "shaded", "plugin")
