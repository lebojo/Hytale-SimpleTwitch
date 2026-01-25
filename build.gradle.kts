plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.3.1"
    id("run-hytale")
    kotlin("jvm")
}

group = findProperty("pluginGroup") as String? ?: "ch.lebojo"
version = findProperty("pluginVersion") as String? ?: "2.0.0"
description = findProperty("pluginDescription") as String? ?: "The easiest way to integrate twitch into Hytale"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Hytale Server API (provided by server at runtime)
    compileOnly(files("libs/HytaleServer.jar"))

    // Twitch
    implementation("io.github.xanthic.cache:cache-provider-caffeine:0.3.0")
    implementation("org.slf4j:slf4j-simple:1.7.36")
    implementation("com.github.twitch4j:twitch4j:1.25.0")
    
    // Common dependencies (will be bundled in JAR)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:24.1.0")
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation(kotlin("stdlib-jdk8"))
}

// Configure server testing
runHytale {
    jarUrl = "libs/HytaleServer.jar"
    assetsPath = "libs/Assets.zip"
}

tasks {
    // Configure Java compilation
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }
    
    // Configure resource processing
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        
        // Replace placeholders in manifest.json
        val props = mapOf(
            "group" to project.group,
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(props)
        
        filesMatching("manifest.json") {
            expand(props)
        }
    }
    
    // Configure ShadowJar (bundle dependencies)
    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")

        mergeServiceFiles()
        
        // Relocate dependencies to avoid conflicts
        relocate("com.google.gson", "ch.lebojo.libs.gson")
        
        // Minimize JAR size (removes unused classes)
        // minimize()
    }
    
    // Configure tests
    test {
        useJUnitPlatform()
    }
    
    // Make build depend on shadowJar
    build {
        dependsOn(shadowJar)
    }
}

// Configure Java toolchain
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
