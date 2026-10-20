import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
}

abstract class GenerateWebConfiguration : DefaultTask() {
    @get:Input
    abstract val useMockData: Property<Boolean>

    @get:Input
    abstract val apiBaseUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val packageDirectory = outputDirectory.dir("com/project/whobusy/di").get().asFile
        packageDirectory.mkdirs()
        val escapedUrl = apiBaseUrl.get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        packageDirectory.resolve("WebBuildConfiguration.kt").writeText(
            """
            package com.project.whobusy.di

            internal object WebBuildConfiguration {
                const val useMockData: Boolean = ${useMockData.get()}
                const val apiBaseUrl: String = "$escapedUrl"
            }
            """.trimIndent() + "\n",
        )
    }
}

val generateWebConfiguration = tasks.register<GenerateWebConfiguration>("generateWebConfiguration") {
    useMockData.set(providers.gradleProperty("whoBusy.web.useMockData").map(String::toBooleanStrict).orElse(false))
    apiBaseUrl.set(providers.gradleProperty("whoBusy.web.apiBaseUrl").orElse(""))
    outputDirectory.set(layout.buildDirectory.dir("generated/webConfiguration"))
}

kotlin {
    js {
        browser()
        nodejs {
            testTask {
                useMocha {
                    timeout = "30s"
                }
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    android {
        namespace = "com.project.whobusy.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.material3)
            api(libs.compose.ui)
            api(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            api(libs.multiplatform.lifecycle.viewmodel.compose)
            api(libs.multiplatform.lifecycle.runtime.compose)
            api(libs.multiplatform.lifecycle.viewmodel.savedstate)
            api(libs.multiplatform.lifecycle.viewmodel.navigation3)
            api(libs.multiplatform.navigation3.ui)
            api(libs.androidx.navigation3.runtime)
            api(libs.multiplatform.material.icons)
            api(libs.metro.viewmodel.compose)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
        }
        jsMain.dependencies {
            implementation(npm("@js-joda/timezone", libs.versions.jsJodaTimezone.get()))
        }
        wasmJsMain.dependencies {
            implementation(npm("@js-joda/timezone", libs.versions.jsJodaTimezone.get()))
        }
        matching { it.name == "webMain" }.configureEach {
            kotlin.srcDir(generateWebConfiguration)
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.project.whobusy.resources"
    generateResClass = always
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
