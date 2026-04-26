import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")

}

val libsCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

android {
    namespace = "com.kynzai.petmates"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.kynzai.petmates"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libsCatalog.findLibrary("androidx-lifecycle-viewmodel-ktx").get())
    implementation(libsCatalog.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    implementation(libs.androidx.activity.compose)

    // Needed because app-level Hilt modules reference Ktor types (HttpClient).
    implementation(libsCatalog.findLibrary("ktor-client-core").get())
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation(libsCatalog.findLibrary("androidx-compose-material-icons-extended").get())
    implementation(libsCatalog.findLibrary("androidx-navigation-compose").get())
    implementation(libsCatalog.findLibrary("androidx-hilt-navigation-compose").get())
    implementation(libsCatalog.findLibrary("hilt-android").get())
    add("ksp", libsCatalog.findLibrary("hilt-compiler").get())

    implementation(project(":domain"))
    implementation(project(":data"))


}
