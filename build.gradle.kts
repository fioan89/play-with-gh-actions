import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfoRt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.changelog)
}

group = "org.fioan89"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

val extension = ExtensionJson(
    id = properties("group"),

    version = properties("version"),
    meta = ExtensionJsonMeta(
        name = "Coder",
        description = "Connects your JetBrains IDE to Coder workspaces",
        vendor = "Coder Technologies, Inc.",
        url = "https://github.com/coder/coder-jetbrains-toolbox",
    )
)

changelog {
    version.set(extension.version)
    groups.set(emptyList())
    title.set("Coder Toolbox Plugin Changelog")
}


tasks.compileKotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.test {
    useJUnitPlatform()
}


tasks.jar {
    archiveBaseName.set(extension.id)
}

val copyPlugin by tasks.creating(Sync::class.java) {
    dependsOn(tasks.jar)

    fromCompileDependencies()
}

fun CopySpec.fromCompileDependencies() {
    from(tasks.jar)

    from("src/main/resources") {
        include("dependencies.json")
    }
    from("src/main/resources") {
        include("icon.svg")
        rename("icon.svg", "pluginIcon.svg")
    }

    // Copy dependencies, excluding those provided by Toolbox.
    from(
        configurations.compileClasspath.map { configuration ->
            configuration.files.filterNot { file ->
                listOf(
                    "kotlin",
                    "remote-dev-api",
                    "core-api",
                    "ui-api",
                    "annotations",
                    "localization-api",
                    "slf4j-api"
                ).any { file.name.contains(it) }
            }
        },
    )
}

/**
 * Useful when doing manual local install.
 */
val pluginPrettyZip by tasks.creating(Zip::class) {
    archiveBaseName.set(properties("name"))
    dependsOn(tasks.jar)

    fromCompileDependencies()
    into(extension.id) // folder like com.coder.toolbox
}

val pluginZip by tasks.creating(Zip::class) {
    dependsOn(tasks.jar)

    fromCompileDependencies()
    archiveBaseName.set(extension.id)
}

tasks.register("cleanAll", Delete::class.java) {
    dependsOn(tasks.clean)
    delete()
}

val publishPlugin by tasks.registering {
    dependsOn(pluginZip)
}

fun properties(key: String) = project.findProperty(key).toString()


// region will be moved to the gradle plugin late
data class ExtensionJsonMeta(
    val name: String,
    val description: String,
    val vendor: String,
    val url: String?,
)

data class ExtensionJson(
    val id: String,
    val version: String,
    val meta: ExtensionJsonMeta,
)
// endregion