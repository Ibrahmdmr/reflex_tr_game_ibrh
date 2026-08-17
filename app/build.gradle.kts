import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.reflex.tr.game.ibrh"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.reflex.tr.game.ibrh"
        minSdk = 25
        targetSdk = 36
        versionCode = 14
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("boolean", "AD_LOGGING_ENABLED", "true")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["admobAppId"] = "ca-app-pub-2483444595618509~3630426306"
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-2483444595618509~3630426306\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-2483444595618509/5335804928\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-2483444595618509/3693129772\"")
            buildConfigField("boolean", "AD_LOGGING_ENABLED", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // R8 output signed with the debug key, for smoke-testing. No applicationIdSuffix:
        // google-services.json registers only com.reflex.tr.game.ibrh.
        create("releaseTest") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            versionNameSuffix = "-releasetest"
            matchingFallbacks += listOf("release")
            // Sample ad units; production ones do not serve to a debug-signed build.
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("boolean", "AD_LOGGING_ENABLED", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    bundle {
        language {
            // Every language must ship in the base APK for the in-app TR/EN switch to work.
            enableSplit = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.ads)
    implementation(libs.play.review.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
