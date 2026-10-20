pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

// The ShopArchive folder (shoparchive.ref says which commit): its version catalog, its shared and client core sources and its strings are used as they are.
// The default is the sibling folder; CI checks it out into shoparchive/ and passes -Pshoparchive=shoparchive.
val shopArchive = rootDir.resolve(providers.gradleProperty("shoparchive").getOrElse("../ShopArchive")).canonicalFile
require(shopArchive.resolve("gradle/libs.versions.toml").isFile) {
    "ShopArchive not found at $shopArchive: check it out there, or pass -Pshoparchive=<folder>"
}
gradle.extra["shopArchive"] = shopArchive

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        // Kotlin, Ktor, serialization, coroutines and datetime match ShopArchive's exactly.
        create("libs") {
            from(files(shopArchive.resolve("gradle/libs.versions.toml")))
        }
        // What only the iOS app uses.
        create("ios") {
            from(files("gradle/ios.versions.toml"))
        }
    }
}

rootProject.name = "ShopArchiveiOS"

include(":clientcore")
