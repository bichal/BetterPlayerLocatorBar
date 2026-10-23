import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.neoforged.moddevgradle.dsl.NeoForgeExtension

plugins {
    id("java")
    id("maven-publish")
    id("fabric-loom") version "1.13.6" apply false
    id("net.neoforged.moddev") version "2.0.148" apply false
}

// One Gradle project per "<minecraft>-<loader>" pair, all sharing the same sources.
val mcVersion = stonecutter.current.version
val isFabric = project.name.endsWith("-fabric")
val loaderName = if (isFabric) "fabric" else "neoforge"

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
    toolchain.languageVersion = JavaLanguageVersion.of(21)
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
    options.release = 21
    options.encoding = "UTF-8"
}

// Loader specific code lives in net.bichal.bplb.platform.<loader>; the other loader's package
// and metadata never reach the compiler or the jar.
val otherLoader = if (isFabric) "neoforge" else "fabric"
sourceSets.main {
    java.exclude("**/platform/$otherLoader/**")
    resources.exclude(if (isFabric) "META-INF/neoforge.mods.toml" else "fabric.mod.json")
}

if (isFabric) {
    apply(plugin = "fabric-loom")
    val loom = extensions.getByType<LoomGradleExtensionAPI>()

    dependencies {
        add("minecraft", "com.mojang:minecraft:$mcVersion")
        add("mappings", loom.officialMojangMappings())
        add("modImplementation", "net.fabricmc:fabric-loader:${property("fabric_loader")}")
        add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${property("fabric_api")}")
        add("modCompileOnly", "com.terraformersmc:modmenu:${property("mod_menu")}")
        add("modLocalRuntime", "com.terraformersmc:modmenu:${property("mod_menu")}")
    }

    loom.runs.named("client") {
        runDir = "../../run/$mcVersion-fabric"
        vmArg("-Dbplb.debug=true")
    }
    loom.runs.named("server") {
        runDir = "../../run/$mcVersion-fabric-server"
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
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml")) {
        expand(props)
    }
}

tasks.jar {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
