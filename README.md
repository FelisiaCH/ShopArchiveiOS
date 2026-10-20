# ShopArchiveiOS

The iPhone app of [ShopArchive](https://github.com/FelisiaCH/ShopArchive): a SwiftUI app that talks to the shop's own server. It shares the client core (the API client, sign-in and the state of every screen) with the Android and Windows apps; `shoparchive.ref` is the ShopArchive commit this app is built against, and CI compiles that code into the `ClientCore` XCFramework the app links.

## Install with AltStore

In AltStore Classic, add this source (Browse, Sources, +):

    https://github.com/FelisiaCH/ShopArchiveiOS/releases/latest/download/source.json

## Build and install from Linux

Needs [xtool](https://github.com/xtool-org/xtool) 1.22.2, the `gh` CLI (logged in) and an iPhone on USB. Kotlin/Native links Apple binaries only on macOS, so the framework comes from CI:

    ./fetch-clientcore.sh      # downloads ClientCore and the app icon that CI built for HEAD (push and wait for CI first)
    xtool dev run --usb

## Build on a Mac

    xcodegen generate
    xcodebuild -project ShopArchive.xcodeproj -scheme ShopArchive -sdk iphonesimulator -configuration Debug CODE_SIGNING_ALLOWED=NO build

`Frameworks/ClientCore.xcframework` must exist first: `./gradlew -Pshoparchive=<ShopArchive folder> :clientcore:assembleClientCoreReleaseXCFramework`, then copy `clientcore/build/XCFrameworks/release/ClientCore.xcframework` to `Frameworks/`. The ShopArchive folder defaults to `../ShopArchive`.

## Release

Push a tag `v*`: CI builds the unsigned `ShopArchive.ipa` and `source.json` and keeps both in a draft GitHub release. Test it, then publish the release.
