plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.urekaapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.urekaapp"
        // minSdk = 24
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        // sourceCompatibility = JavaVersion.VERSION_1_8
        // targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // sourceCompatibility = JavaVersion.VERSION_16
        // targetCompatibility = JavaVersion.VERSION_16
    }

    // IDK why I need this
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.junit.jupiter)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // extra imports for ureka_framework
    implementation(libs.gson) // Gson
    implementation(libs.bcprov.jdk15on)
    implementation(libs.bcpkix.jdk15on)
    implementation(libs.guava)
    implementation(libs.core) // SpongyCastle
    implementation(libs.prov) // SpongyCastle
    implementation("com.madgag.spongycastle:core:1.58.0.0") // SpongyCastle
    implementation("com.madgag.spongycastle:prov:1.58.0.0") // SpongyCastle
    implementation(libs.google.play.nearby) // Nearby
    implementation(libs.play.services.nearby.v1800)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

}