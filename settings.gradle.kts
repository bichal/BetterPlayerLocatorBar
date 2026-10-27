// Managed by bichal-build-logic (shared/mod/settings.gradle.kts): `bichal sync`
// overwrites this file. Everything it does lives in the bichal.settings plugin:
// merging mod.stonecutter.properties.toml with the common TOML, choosing the
// Minecraft versions ([stonecutter], -Pstonecutter.versions=...), the
// Stonecutter tree and early validation.
// Names the build-logic checkout may have: the folder is matched ignoring case and
// separators, so PanzerBuildLogic, panzer-build-logic and BICHAL_BUILD_LOGIC all match.
// Keep in step with net.bichal.gradle.BuildLogicNames.
val buildLogicNames = setOf("bichalbuildlogic", "panzerbuildlogic", "bichallogic", "panzerlogic")

fun isBuildLogicFolder(dir: File): Boolean =
    dir.isDirectory && dir.resolve("settings.gradle.kts").isFile &&
        dir.name.lowercase().filter { it.isLetterOrDigit() } in buildLogicNames

// The build logic is a sibling checkout, not a published plugin. BICHAL_BUILD_LOGIC_DIR
// points at it explicitly; otherwise any sibling folder with an accepted name is used.
val buildLogic: File = System.getenv("BICHAL_BUILD_LOGIC_DIR")?.let { file(it) }?.takeIf { it.resolve("settings.gradle.kts").isFile }
    ?: settingsDir.parentFile?.listFiles()?.firstOrNull { isBuildLogicFolder(it) }
    ?: throw GradleException(
        "The shared build logic was not found next to this mod (looked in ${settingsDir.parentFile}).\n" +
            "Clone it into that folder under any of these names: bichal-build-logic, PanzerBuildLogic,\n" +
            "or set BICHAL_BUILD_LOGIC_DIR to its path.\n" +
            "  git clone https://github.com/bichal/BichalBuildLogic.git"
    )
// bichal.settings reads this to find the common TOML.
extra["bichal.buildLogicDir"] = buildLogic

pluginManagement {
    includeBuild(buildLogic)

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
    id("bichal.settings")
}
