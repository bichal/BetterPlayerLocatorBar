import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.neoforged.moddevgradle.dsl.NeoForgeExtension

plugins {
    id("java")
    id("maven-publish")
    id("fabric-loom") version "1.16.3" apply false
    id("net.fabricmc.fabric-loom") version "1.16.3" apply false
    id("net.neoforged.moddev") version "2.0.148" apply false
}

// One Gradle project per "<minecraft>-<loader>" pair, all sharing the same sources.
val mcVersion = stonecutter.current.version
val isFabric = project.name.endsWith("-fabric")
val loaderName = if (isFabric) "fabric" else "neoforge"
val waypoints = stonecutter.eval(mcVersion, ">=1.21.6")
// 26.1 ships unobfuscated: no mappings, no remapping, Java 25
val unobfuscated = stonecutter.eval(mcVersion, ">=26.1")
val javaVersion = if (unobfuscated) 25 else 21

val modId: String by project
val modVersion: String by project
val modName: String by project
val modDescription: String by project

version = "$modVersion+$mcVersion-$loaderName"
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.shedaniel.me/") { name = "Shedaniel" }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("bplb.bench", providers.systemProperty("bplb.bench").getOrElse("false"))
    workingDir = layout.buildDirectory.dir("test-work").get().asFile.also { it.mkdirs() }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion
    options.encoding = "UTF-8"
}

// Loader specific code lives in net.bichal.bplb.platform.<loader>; the other loader's package
// and metadata never reach the compiler or the jar.
val otherLoader = if (isFabric) "neoforge" else "fabric"
sourceSets.main {
    java.exclude("**/platform/$otherLoader/**")
    if (waypoints) {
        java.exclude("**/server/**", "**/network/**", "**/mixin/HotbarMixin.java")
    } else {
        java.exclude("**/WaypointSource.java", "**/mixin/WaypointBarMixin.java")
    }
    resources.exclude(if (isFabric) "META-INF/neoforge.mods.toml" else "fabric.mod.json")
}

sourceSets.test {
    if (waypoints) java.exclude("**/network/**")
    else java.exclude("**/WaypointSourceTest.java")
}

if (isFabric) {
    apply(plugin = if (unobfuscated) "net.fabricmc.fabric-loom" else "fabric-loom")
    val loom = extensions.getByType<LoomGradleExtensionAPI>()

    // Unobfuscated versions use the plain Gradle configurations and need no mappings.
    val api = if (unobfuscated) "implementation" else "modImplementation"
    val compileOnly = if (unobfuscated) "compileOnly" else "modCompileOnly"
    val localRuntime = if (unobfuscated) "runtimeOnly" else "modLocalRuntime"
    dependencies {
        add("minecraft", "com.mojang:minecraft:$mcVersion")
        if (!unobfuscated) add("mappings", loom.officialMojangMappings())
        add(api, "net.fabricmc:fabric-loader:${property("fabric_loader")}")
        add(api, "net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}")
        add(compileOnly, "com.terraformersmc:modmenu:${property("mod_menu")}")
        add(localRuntime, "com.terraformersmc:modmenu:${property("mod_menu")}")
    }

    loom.runs.named("client") {
        runDir = "../../run/$mcVersion-fabric"
        vmArg("-Dbplb.debug=true")
        providers.gradleProperty("bplb.addMods").orNull?.let { vmArg("-Dfabric.addMods=$it") }
        // -Pbplb.quickplay=<host> joins that server straight away (dev testing)
        providers.gradleProperty("bplb.quickplay").orNull?.let { programArgs("--quickPlayMultiplayer", it) }
    }
    loom.runs.named("server") {
        runDir = "../../run/$mcVersion-fabric-server"
        // -Pbplb.addMods=<jar>[,<jar>] loads extra mods in dev runs (for example Carpet fake players)
        providers.gradleProperty("bplb.addMods").orNull?.let { vmArg("-Dfabric.addMods=$it") }
    }
} else {
    apply(plugin = "net.neoforged.moddev")
    extensions.configure<NeoForgeExtension>("neoForge") {
        version = property("neoforge") as String
        runs {
            register("client") {
                client()
                gameDirectory.set(rootProject.file("run/$mcVersion-neoforge"))
                // -Pbplb.quickplay=<host> joins that server straight away (dev testing)
                providers.gradleProperty("bplb.quickplay").orNull?.let {
                    programArguments.addAll("--quickPlayMultiplayer", it)
                }
            }
            register("server") {
                server()
                gameDirectory.set(rootProject.file("run/$mcVersion-neoforge-server"))
                programArgument("--nogui")
            }
        }
        mods {
            register(modId) {
                sourceSet(sourceSets.main.get())
            }
        }
        unitTest {
            enable()
            testedMod = mods.getByName(modId)
        }
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to modVersion,
        "mc_range" to project.property("mc_range") as String,
        "loader_range" to project.property("loader_range") as String,
        "mod_id" to modId,
        "mod_name" to modName,
        "mod_description" to modDescription,
        "client_mixins" to if (waypoints) "\"ClientPlayerEntityMixin\", \"WaypointBarMixin\"" else "\"ClientPlayerEntityMixin\", \"HotbarMixin\"",
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "bplb.mixins.json")) {
        expand(props)
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
