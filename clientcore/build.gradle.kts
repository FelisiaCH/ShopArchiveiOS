import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(ios.plugins.skie)
}

// The client core of the iOS app: ShopArchive's :shared and :clientCore commonMain (and their commonTest) compiled together with this folder's iosMain.
// Nothing is copied; the sources are read from the ShopArchive folder that settings.gradle.kts found.
val shopArchive = gradle.extra["shopArchive"] as File

kotlin {
    // The static framework the app links; SKIE turns Flow, suspend and sealed types into Swift's AsyncSequence, async and enum.
    // The XCFramework (device + simulator) is what CI hands to Xcode and, through fetch-clientcore.sh, to xtool on Linux.
    val xcf = XCFramework("ClientCore")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ClientCore"
            isStatic = true
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(shopArchive.resolve("shared/src/commonMain/kotlin"))
            kotlin.srcDir(shopArchive.resolve("clientCore/src/commonMain/kotlin"))
            dependencies {
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                api(libs.ktor.client.core)
                implementation(libs.ktor.client.websockets)
            }
        }
        commonTest {
            kotlin.srcDir(shopArchive.resolve("shared/src/commonTest/kotlin"))
            kotlin.srcDir(shopArchive.resolve("clientCore/src/commonTest/kotlin"))
            dependencies {
                implementation(kotlin("test"))
            }
        }
        iosMain.dependencies {
            // Only the iOS app uses the Darwin engine, so it is not in ShopArchive's catalog; its version is Ktor's there.
            implementation("io.ktor:ktor-client-darwin:${libs.versions.ktor.get()}")
        }
    }
}
