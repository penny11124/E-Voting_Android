plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.urekaapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.urekaapp"
        minSdk = 24
        targetSdk = 34
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.junit.jupiter)
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
}