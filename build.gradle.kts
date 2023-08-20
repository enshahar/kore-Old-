plugins {
    kotlin("multiplatform") version "1.9.0"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm{
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
            }
        }
    }
    @Suppress("OPT_IN_USAGE")
    wasm {
        binaries.executable()
        browser {

        }
    }
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val wasmMain by getting
        val wasmTest by getting
    }
}