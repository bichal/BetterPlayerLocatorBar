import net.bichal.gradle.BichalModExtension
import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

// Shared build logic: bichal-build-logic's bichal.mod (NeoForge or Fabric node by name,
// dependencies, resources, legal files, buildAndCollect). Only what is specific to
// Better Player Locator Bar belongs here.
plugins {
    id("bichal.mod")
    idea
}

val modProps = extensions.getByType(BichalModExtension::class.java).props
val stonecutter = extensions.getByType(StonecutterBuildExtension::class.java)
val mcParsed = stonecutter.current.parsed

// 1.21.6 added the vanilla locator bar (waypoints): from there the mod customises it
// instead of tracking players itself, with no server code or network.
val waypoints = mcParsed >= "1.21.6"
val unobfuscated = mcParsed >= "26.1"

repositories {
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Loader specific code lives in net.bichal.bplb.platform.<loader>; the other loader's package
// never reaches the compiler. The legacy (own bar) and waypoint modes have their own classes.
val otherLoader = if (modProps.isFabric) "neoforge" else "fabric"
sourceSets.main {
    java.exclude("**/platform/$otherLoader/**")
    if (waypoints) {
        java.exclude("**/server/**", "**/network/**", "**/mixin/HotbarMixin.java")
    } else {
        java.exclude(
            "**/WaypointSource.java", "**/mixin/WaypointBarMixin.java",
            "**/mixin/ServerWaypointManagerMixin.java", "**/waypoint/ServerOps.java"
        )
    }
}

sourceSets.test {
    if (waypoints) java.exclude("**/network/**")
    else java.exclude("**/WaypointSourceTest.java")
}

tasks.test {
    systemProperty("bplb.bench", providers.systemProperty("bplb.bench").getOrElse("false"))
    workingDir = layout.buildDirectory.dir("test-work").get().asFile.also { it.mkdirs() }
}

// The mixin that draws the locator bar depends on the mode.
tasks.processResources {
    val locatorMixin = if (waypoints) "WaypointBarMixin" else "HotbarMixin"
    // Only the waypoint mode has a server mixin (it speeds up the vanilla waypoint manager).
    val serverMixin = if (waypoints) "\"ServerWaypointManagerMixin\"" else ""
    inputs.property("locator_mixin", locatorMixin)
    inputs.property("server_mixin", serverMixin)
    filesMatching("bplb.mixins.json") {
        filter { it.replace("@LOCATOR_MIXIN@", locatorMixin).replace("\"@SERVER_MIXIN@\"", serverMixin) }
    }
}

val quickplay: String? = providers.gradleProperty("bplb.quickplay").orNull
// -Pbplb.addMods=<jar>[,<jar>] loads extra mods in Fabric dev runs (for example Carpet fake players)
val addMods: String? = providers.gradleProperty("bplb.addMods").orNull

if (modProps.isFabric) {
    val modMenu = modProps.req(project, "mod_menu")
    // Unobfuscated versions use the plain Gradle configurations.
    val compileOnly = if (unobfuscated) "compileOnly" else "modCompileOnly"
    val localRuntime = if (unobfuscated) "runtimeOnly" else "modLocalRuntime"
    dependencies {
        add(compileOnly, "com.terraformersmc:modmenu:$modMenu")
        add(localRuntime, "com.terraformersmc:modmenu:$modMenu")
    }

    val loom = extensions.getByType(LoomGradleExtensionAPI::class.java)
    afterEvaluate {
        loom.runs.named("client") {
            vmArg("-Dbplb.debug=true")
            addMods?.let { vmArg("-Dfabric.addMods=$it") }
            quickplay?.let { programArgs("--quickPlayMultiplayer", it) }
        }
        loom.runs.named("server") {
            runDir("run-server")
            addMods?.let { vmArg("-Dfabric.addMods=$it") }
        }
    }
} else {
    val runDir = rootProject.file("versions/${modProps.currentVersion}/run")
    extensions.configure<net.neoforged.moddevgradle.dsl.NeoForgeExtension>("neoForge") {
        runs {
            register("client") {
                client()
                gameDirectory = runDir
                quickplay?.let { programArguments.addAll("--quickPlayMultiplayer", it) }
            }
            register("server") {
                server()
                gameDirectory = rootProject.file("versions/${modProps.currentVersion}/run-server")
                programArgument("--nogui")
            }
        }
        unitTest {
            enable()
            testedMod = mods.getByName(modProps.modId)
        }
    }
}
