import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

val libsCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

fun configValue(vararg names: String): String =
    names.firstNotNullOfOrNull { name ->
        providers.gradleProperty(name).orNull
            ?: System.getenv(name)
    }.orEmpty()

fun buildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.kynzai.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 26
        /*
         * Android должен принимать переменные как из backend/.env, так и из web/.env.
         * Важно: SUPABASE_KEY намеренно НЕ читаем, потому что под этим именем часто
         * передают service-role secret key. В мобильное приложение можно вшивать
         * только publishable/anon key.
         */
        val supabaseUrl = configValue("SUPABASE_URL", "VITE_SUPABASE_URL")
        val supabaseAnonKey = configValue(
            "SUPABASE_ANON_KEY",
            "SUPABASE_PUBLISHABLE_KEY",
            "VITE_SUPABASE_KEY",
        )
        val apiBaseUrl = configValue("API_BASE_URL", "API_URL", "VITE_API_URL")
        buildConfigField("String", "SUPABASE_URL", buildConfigString(supabaseUrl))
        buildConfigField("String", "SUPABASE_ANON_KEY", buildConfigString(supabaseAnonKey))
        buildConfigField("String", "API_BASE_URL", buildConfigString(apiBaseUrl))
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
    implementation(libsCatalog.findLibrary("ktor-client-content-negotiation").get())
    implementation(libsCatalog.findLibrary("ktor-serialization-kotlinx-json").get())
    implementation(libsCatalog.findLibrary("kotlinx-serialization-json").get())
    testImplementation(libsCatalog.findLibrary("ktor-client-mock").get())
    testImplementation(libsCatalog.findLibrary("org-json").get())

    // Keep Hilt runtime annotations available for @Inject/@Singleton used in this module.
    implementation(libsCatalog.findLibrary("hilt-android").get())

    implementation(project(":domain"))

}
