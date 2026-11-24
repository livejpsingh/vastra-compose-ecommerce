# Vastra

Vastra is an Android fashion-store demo built with Kotlin and Jetpack Compose. It includes a product catalogue, search and filters, product details, a shopping bag, wishlist, checkout flow, order history, and a local shopper profile.

The catalogue is loaded from DummyJSON. Bag contents, wishlist, profile, address, orders, and the catalogue cache are stored on the device. Checkout records a local demo order; the app does not connect to a payment processor or order-management backend.

## Features

- Browse products, search by title/brand/category, filter and sort results.
- View product images, prices, discounts, stock, and applicable size options.
- Manage bag quantities and wishlist items.
- Validate a delivery address and place a local demo order.
- View saved orders and manage local profile details.
- Load a cached catalogue when the network is unavailable.

## Requirements

- Android Studio with Android SDK Platform 34 installed.
- JDK 17.
- An emulator or device running Android 8.0 (API 26) or later.

The Gradle wrapper downloads the configured Gradle distribution on first use.

## Build and test

From macOS or Linux:

```shell
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

From Windows PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

The debug APK is written to `app/build/outputs/apk/debug/`. The release APK is minified and unsigned; it is not ready for production distribution until signing is configured.

## Project documentation

- [Architecture](docs/architecture.md): app layers, startup, data flow, persistence, and current system boundaries.
- [Developer guide](docs/developer-guide.md): setup, build/test commands, project layout, and guidance for common changes.

## CI/CD

GitHub Actions runs unit tests and builds the debug APK on pushes and pull requests. The APK is retained as a workflow artifact.

Pushing a tag matching `v*` runs the release workflow and publishes the unsigned release APK to GitHub Releases. Configure release signing before using an APK as an installable production release.

## Security notes

- Network requests use HTTPS and cleartext traffic is disabled in the Android manifest.
- Shopper data is stored locally using Android DataStore Preferences. The app does not provide account authentication or payment processing.
- Release builds enable R8 code shrinking and resource shrinking.
- See `app/proguard-rules.pro` for serialization-related keep rules and the manifest backup rules under `app/src/main/res/xml/`.
