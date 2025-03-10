
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

val pluginZip by tasks.creating(Zip::class) {
    destinationDirectory.set(file("build/distributions"))
    archiveBaseName.set("play-with-gh-actions")
    dependsOn(tasks.jar)
    from(tasks.jar)
    from("src/main/resources")
    into("com.fioan89") // folder like com.fioan89
    // Specify the directory where the zip file will be saved
}
