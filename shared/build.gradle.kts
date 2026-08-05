import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.ksp)
  alias(libs.plugins.androidx.room)
  alias(libs.plugins.aboutLibraries)
  id("com.diffplug.spotless") version "8.9.0"
}

spotless {
  kotlin {
    target("src/**/*.kt", "src/**/*.kts", "*.kt", "*.kts")
    ktfmt().googleStyle()
  }
}

kotlin {
  android {
    namespace = "personal.limi.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions {
      jvmTarget = JvmTarget.JVM_25
      freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidResources {
      enable = true
    }

    withHostTest {
      isIncludeAndroidResources = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.material.icons.extended)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.cio)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.androidx.datastore)
      implementation(libs.androidx.datastore.preferences)
      implementation(libs.navigation.compose)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.kotlinx.datetime)
      implementation(libs.pangu)
      implementation(libs.kotlincrypto.hash.sha2)
      implementation(libs.aboutlibraries.core)
      implementation(libs.compose.material.symbols)
    }
    androidMain.dependencies {
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.compose.uiTooling)
      implementation(libs.androidx.room.sqlite.wrapper)
      implementation(libs.play.services.code.scanner)
      implementation(libs.kotlinx.coroutines.play.services)
      implementation(libs.androidx.browser)
      implementation(libs.barcode.scanning)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

dependencies {
  androidRuntimeClasspath(libs.compose.uiTooling)
  add("kspAndroid", libs.androidx.room.compiler)
}

room {
  schemaDirectory("$projectDir/schemas")
}

aboutLibraries {
  export {
    outputPath = file("src/commonMain/composeResources/files/aboutlibraries.json")
  }
}

tasks
  .matching {
    it.name == "generateComposeResClass" || it.name == "copyNonXmlValueResourcesForCommonMain"
  }
  .configureEach {
    dependsOn("exportLibraryDefinitions")
  }
