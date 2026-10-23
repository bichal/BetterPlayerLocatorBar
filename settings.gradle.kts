pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    create(rootProject) {
        // Project name is "<minecraft>-<loader>"; the second argument is the Minecraft version.
        // NeoForge skipped some Minecraft versions, so every target lists its own loaders.
        val targets = mapOf(
            "1.21.1" to listOf("fabric", "neoforge"),
            "1.21.3" to listOf("fabric", "neoforge"),
            "1.21.4" to listOf("fabric", "neoforge"),
            "1.21.5" to listOf("fabric", "neoforge"),
            "1.21.6" to listOf("fabric"),
            "1.21.7" to listOf("fabric"),
            "1.21.8" to listOf("fabric", "neoforge"),
            "1.21.9" to listOf("fabric"),
            "1.21.10" to listOf("fabric", "neoforge"),
            "1.21.11" to listOf("fabric", "neoforge"),
            "26.1.2" to listOf("fabric", "neoforge"),
            "26.2" to listOf("fabric", "neoforge"),
            "26.3" to listOf("fabric", "neoforge"),
        )
        for ((mc, loaders) in targets) {
            for (loader in loaders) {
                version("$mc-$loader", mc)
            }
        }
        vcsVersion = "1.21.1-fabric"
    }
}

rootProject.name = "BetterPlayerLocatorBar"
