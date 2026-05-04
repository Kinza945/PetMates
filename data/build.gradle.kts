import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.android.library)
}

val libsCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

android {
    namespace = "com.kynzai.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
        val supabaseUrl = providers.gradleProperty("SUPABASE_URL").orNull ?: System.getenv("SUPABASE_URL") ?: ""
        val supabaseAnonKey = providers.gradleProperty("SUPABASE_ANON_KEY").orNull ?: System.getenv("SUPABASE_ANON_KEY") ?: ""
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        val useMocks = (providers.gradleProperty("USE_MOCKS").orNull ?: System.getenv("USE_MOCKS") ?: "true")
        buildConfigField("boolean", "USE_MOCKS", useMocks)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libsCatalog.findLibrary("ktor-client-core").get())
    implementation(libsCatalog.findLibrary("ktor-client-okhttp").get())
    implementation(libsCatalog.findLibrary("ktor-client-logging").get())
    testImplementation(libsCatalog.findLibrary("ktor-client-mock").get())
    testImplementation(libsCatalog.findLibrary("org-json").get())

    // Keep Hilt runtime annotations available for @Inject/@Singleton used in this module.
    implementation(libsCatalog.findLibrary("hilt-android").get())

    implementation(project(":domain"))

}
