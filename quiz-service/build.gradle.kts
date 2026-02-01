plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "com.lectureai"
version = "1.0.0"

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.3"
val koogVersion = "0.6.1"
val kotlinxSerializationVersion = "1.7.3"
val logbackVersion = "1.5.15"

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    
    // Ktor Client (for direct HTTP calls if needed)
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    
    // Koog Agent Framework
    implementation("ai.koog:koog-agents:$koogVersion")
    implementation("ai.koog:prompt-executor-anthropic-client:$koogVersion")
    implementation("ai.koog:prompt-executor-llms-all:$koogVersion")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.lectureai.quiz.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}
