import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))

            implementation(libs.compose.ui)
        }
    }
}

tasks.register<Sync>("exportFunProjectFrontend") {
    group = "distribution"
    description = "Builds the production Web app for committing into FunProject/frontend."
    dependsOn("wasmJsBrowserDistribution")

    from(layout.buildDirectory.dir("dist/wasmJs/productionExecutable")) {
        into("frontend/static")
    }
    from(layout.projectDirectory.dir("deployment/funproject")) {
        into("frontend")
    }
    into(layout.buildDirectory.dir("deploy"))
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.FAIL
}
