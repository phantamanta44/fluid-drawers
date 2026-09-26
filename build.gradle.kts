plugins {
    id("java-library")
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.2"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
}

val modId: String = "fluiddrawers"
version = "1.0.7"
group = "xyz.phanta.fluiddrawers"

val minecraftVersion: String = "1.12.2"
val corePluginClass: String = "xyz.phanta.fluiddrawers.coremod.FluidDrawersCoreMod"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
        vendor = JvmVendorSpec.AZUL
    }
}

minecraft {
    mcVersion = minecraftVersion
    mcpMappingChannel = "stable"
    mcpMappingVersion = "39"

    useDependencyAccessTransformers = true

    injectedTags.put("MOD_ID", modId)
    injectedTags.put("VERSION", project.version)

    username = "Player"
}

tasks.injectTags {
    outputClassName = "${project.group}.FdConst"
}

tasks.applyJST {
    // https://github.com/GTNewHorizons/RetroFuturaGradle/issues/101
    javaLauncher = minecraft.getToolchainLauncher(project, 25)
}

tasks.runClient {
    extraJvmArgs.add("-Dfml.coreMods.load=$corePluginClass")
}

tasks.runServer {
    extraJvmArgs.add("-Dfml.coreMods.load=$corePluginClass")
}

repositories {
    mavenLocal()
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com/")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    api("io.github.phantamanta44.libnine:libnine-1.12.2:1.2.2")
    api(rfg.deobf("curse.maven:chameleon-230497:2450900")) // 4.1.3
    api(rfg.deobf("curse.maven:storage-drawers-223852:2952606")) // 5.4.2
    implementation(rfg.deobf("curse.maven:framed-compacting-drawers-376351:3015136")) // 1.2.7
}

tasks.processResources {
    inputs.property("modId", modId)
    inputs.property("modVersion", project.version)
    inputs.property("mcVersion", minecraftVersion)

    filesMatching("mcmod.info") {
        expand(
            "modId" to modId,
            "modVersion" to project.version,
            "mcVersion" to minecraftVersion
        )
    }
}

tasks.jar {
    archiveBaseName = "$modId-$minecraftVersion"
    manifest {
        attributes(
            "FMLCorePluginContainsFMLMod" to "true",
            "FMLCorePlugin" to corePluginClass
        )
    }
}
