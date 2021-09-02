plugins {
    kotlin("multiplatform") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.dokka") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    `maven-publish`
    java
}

repositories {
    mavenCentral()

    //maven(url = "https://jitpack.io")
}

kotlin {
    jvm {
        withJava()
    }
    js {
        nodejs()
    }
    mingwX64()
    linuxX64()
    macosX64()

    sourceSets {
        val ktorVersion = "1.6.2"

        val commonMain by getting {
            dependencies {
                //compileOnly(platform("org.jetbrains.kotlin:kotlin-bom"))
                implementation(kotlin("stdlib-common"))

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-java:$ktorVersion")

                val logbackVersion = "1.2.5"
                compileOnly("ch.qos.logback:logback-core:$logbackVersion")
                compileOnly("ch.qos.logback:logback-classic:$logbackVersion")

                implementation("org.jetbrains:annotations:20.1.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))

                val logbackVersion = "1.2.5"
                runtimeOnly("ch.qos.logback:logback-core:$logbackVersion")
                runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

tasks.register("jitpackBuild") {
    dependsOn(
        // clean
        "clean",
        // java
        //"jar",
        // kotlin jvm
        //"jvmJar",
        // maven
        "publishToMavenLocal"
    )
}
