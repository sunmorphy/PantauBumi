import com.android.build.api.variant.impl.VariantOutputImpl
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.andikas.pantaubumi"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.andikas.pantaubumi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "http://localhost:8000")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(localProperties.getProperty("debugStoreFile") ?: "")
            storePassword = localProperties.getProperty("debugStorePassword") ?: ""
            keyAlias = localProperties.getProperty("debugKeyAlias") ?: ""
            keyPassword = localProperties.getProperty("debugKeyPassword") ?: ""
        }
        create("release") {
            storeFile = file(localProperties.getProperty("releaseStoreFile") ?: "")
            storePassword = localProperties.getProperty("releaseStorePassword") ?: ""
            keyAlias = localProperties.getProperty("releaseKeyAlias") ?: ""
            keyPassword = localProperties.getProperty("releaseKeyPassword") ?: ""
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"

            resValue("string", "app_name", "Debug PantauBumi")

            val baseUrl = localProperties.getProperty("BASE_URL_DEBUG")
                ?: providers.gradleProperty("BASE_URL_DEBUG").getOrElse("http://localhost:8000")

            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            resValue("string", "app_name", "PantauBumi")

            val baseUrl = localProperties.getProperty("BASE_URL")
                ?: providers.gradleProperty("BASE_URL").getOrElse("http://localhost:8000")

            buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                val version = output.versionName.get() ?: "1.0.0"
                val name = "PantauBumi-${variant.name}-v$version.apk"

                output.outputFileName.set(name)
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material) // Added for PullRefresh
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(libs.play.services.location)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    implementation(libs.maplibre.android)

    implementation(libs.coil.compose)

    implementation(libs.accompanist.permissions)

    implementation(libs.datastore.preferences)

    implementation(libs.androidx.security.crypto)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.threetenbp)

    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
