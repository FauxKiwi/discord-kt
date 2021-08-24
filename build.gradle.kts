import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.dokka") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    `maven-publish`
    java
}

repositories {
    mavenCentral()

    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(platform("org.jetbrains.kotlin:kotlin-bom"))
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.2")

    val ktor_version = "1.6.2"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")

    val logback_version = "1.2.5"
    implementation("ch.qos.logback:logback-core:$logback_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks {
    // Set name, minimize, and merge service files
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set(project.name)
        mergeServiceFiles()
        minimize()
    }

    test { useJUnitPlatform() }

    // Make build depend on shadowJar as shading dependencies will most likely be required.
    build { dependsOn(shadowJar) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "11" }
val compileKotlin: KotlinCompile by tasks

/*compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}*/

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.properties["group"] as? String?
            artifactId = project.name
            version = project.properties["version"] as? String?

            from(components["java"])
        }
    }
}
