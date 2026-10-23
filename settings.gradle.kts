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
        for (mc in listOf("1.21.1", "1.21.3")) {
            for (loader in listOf("fabric", "neoforge")) {
                version("$mc-$loader", mc)
            }
        }
        vcsVersion = "1.21.1-fabric"
    }
}

rootProject.name = "BetterPlayerLocatorBar"
