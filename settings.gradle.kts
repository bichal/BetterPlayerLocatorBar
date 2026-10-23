// Managed by panzer-build-logic (shared/mod/settings.gradle.kts): `panzer sync`
// overwrites this file. Everything it does lives in the panzer.settings plugin:
// merging mod.stonecutter.properties.toml with the common TOML, choosing the
// Minecraft versions ([stonecutter], -Pstonecutter.versions=...), the
// Stonecutter tree and early validation.
pluginManagement {
    // The build logic is a sibling checkout, not a published plugin. Say how to
    // get it instead of failing later with "plugin panzer.settings not found".
    val buildLogic = settingsDir.resolve("../panzer-build-logic")
    if (!buildLogic.resolve("settings.gradle.kts").isFile) {
        throw GradleException(
            "panzer-build-logic not found at ${buildLogic.normalize()}.\n" +
                "This mod builds with the shared build logic checked out next to it:\n" +
                "  git clone https://github.com/PanzerDevOrg/PanzerBuildLogic.git ${buildLogic.normalize()}\n" +
                "(see \"Building\" in this mod's README)."
        )
    }
    includeBuild("../panzer-build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("panzer.settings")
}
