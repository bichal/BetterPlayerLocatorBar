// Managed by bichal-build-logic (shared/mod/settings.gradle.kts): `bichal sync`
// overwrites this file. Everything it does lives in the bichal.settings plugin:
// merging mod.stonecutter.properties.toml with the common TOML, choosing the
// Minecraft versions ([stonecutter], -Pstonecutter.versions=...), the
// Stonecutter tree and early validation.
pluginManagement {
    // The build logic is a sibling checkout, not a published plugin. BICHAL_BUILD_LOGIC_DIR
    // points at it explicitly; otherwise any sibling folder whose name is accepted is used.
    // Declared here, not at script level: Gradle evaluates this block before the rest of the
    // script. Keep the set in step with net.bichal.gradle.BuildLogicNames (tools/test_layout.py
    // checks it).
    val buildLogicNames = setOf("bichalbuildlogic", "panzerbuildlogic", "bichallogic", "panzerlogic")
    val buildLogic: File = System.getenv("BICHAL_BUILD_LOGIC_DIR")?.let { File(it) }
        ?.takeIf { it.resolve("settings.gradle.kts").isFile }
        ?: settingsDir.parentFile?.listFiles()?.firstOrNull { dir ->
            dir.isDirectory && dir.resolve("settings.gradle.kts").isFile &&
                dir.name.lowercase().filter { it.isLetterOrDigit() } in buildLogicNames
        }
        ?: throw GradleException(
            "The shared build logic was not found next to this mod (looked in ${settingsDir.parentFile}).\n" +
                "Clone it into that folder under any of these names: bichal-build-logic, PanzerBuildLogic,\n" +
                "or set BICHAL_BUILD_LOGIC_DIR to its path.\n" +
                "  git clone https://github.com/bichal/BichalBuildLogic.git"
        )
    // A path string: with a File, Gradle 9 does not find the plugin inside the included build.
    includeBuild(buildLogic.absolutePath)

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
