/*
 *     Copyright (C) 2022-present Techlore
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "tech.techlore.plexus"
    compileSdk = 34

    defaultConfig {
        applicationId = "tech.techlore.plexus"
        minSdk = 23
        targetSdk = 34
        versionCode = 207
        versionName = "2.0.7"
        setProperty("archivesBaseName", "Plexus_v$versionName")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            vcsInfo.include = false // https://f-droid.org/docs/Reproducible_Builds/#vcs-info
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        /*getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }*/
    }
    
    dependenciesInfo {
        includeInApk = false // Disables dependency metadata when building APKs.
        includeInBundle = false // Disables dependency metadata when building Android App Bundles.
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.bundles.androidxCoreComponents)
    implementation(libs.material3)
    implementation(libs.bundles.koin)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.bundles.navigation)
    implementation(libs.jackson.kotlin)
    implementation(libs.bundles.retrofit2)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil)
    implementation(libs.androidFastScrollKt)
    implementation(libs.jsoup)
    implementation(libs.lottie)
}