# Vastra — Production-Grade Fashion Store Android App

A production-level Android e-commerce shopping application written in Kotlin with Jetpack Compose (Material 3).
It implements an architecture designed for high performance, security, offline resilience, and delightful shopper experience: catalogue, search and multi-attribute filters, product detail with gallery pager, bag, wishlist, checkout with input validation, order history, and profile management.

---

## 1. Production Architecture & Highlights

| Domain | Implementation Details |
| --- | --- |
| **UI & Presentation** | Jetpack Compose (Material 3), Edge-to-Edge display, Reactive `StateFlow` unidirectional data flow (UDF), and edge-to-edge system insets. |
| **Dependency Injection** | Manual App Container pattern (`AppContainer`), providing clean singleton lifecycle management without reflection overhead. |
| **Network & API** | Retrofit 2 + Gson with OkHttp client, connection pooling, 20s timeouts, automatic retry on failure, and environment-aware logging (`HttpLoggingInterceptor.Level.BASIC` in Debug, `Level.NONE` in Release). |
| **Offline-First Resilience** | Two-tiered caching: in-memory repository cache plus persistent on-disk JSON cache in DataStore. Network failure automatically falls back to cached products without breaking user experience. |
| **Image Loading & Cache** | Coil with custom `ImageLoaderFactory`, crossfade animations, 25% memory cache cap, and disk caching (`cacheDir/image_cache`). |
| **Security & Privacy** | R8 code shrinking and resource minification enabled for release builds (`isMinifyEnabled = true`, `isShrinkResources = true`). Comprehensive ProGuard keep rules for models, serializers, and DataStore structures. Cleartext traffic disabled (`usesCleartextTraffic="false"`). Backup rules configured to prevent leakage of PII/order details to unencrypted cloud backups. Removed hardcoded personal credentials. |
| **Data Integrity & UX** | Race condition protection during order placement (`isPlacingOrder` guard), sanitized address formatting (automatic trimming, Indian phone and 6-digit PIN validation), out-of-stock validation in bag and catalogue, and background refresh indicator. |
| **Automated Testing** | Unit tests in `app/src/test/` verifying pricing, discount maths, currency formatting, shipping thresholds, and address validation rules. |

---

## 2. Screens & Features

- **Shop:** Browse fashion pieces in a 2-column grid. Search with real-time filtering by title, brand, or category. Sort by rating, price, and discount. Filter by rating, maximum price slider, and in-stock toggle. Visual "Out of stock" badges and live background refresh indicator.
- **Product Detail:** Image pager with dots indicator, brand label, rating, stock status, strike-through MRP with discount percentage, size selector chips, quantity stepper, description, shipping estimate, and sticky add-to-bag / wishlist actions.
- **Bag:** Cart items with thumbnail, size indicator, out-of-stock alerts, quantity stepper (up to 10), remove action, free delivery progress nudge, subtotal, shipping charge, and checkout button.
- **Checkout:** Delivery address form with validation (recipient name, 10-digit Indian phone starting 6–9, street, city, state, 6-digit PIN), pre-filled from saved profile, payment method selection, order summary breakdown, and single-flight order placement.
- **Orders:** Order history with expandable line items, thumbnails, status pill ("Confirmed"), date formatting, and saved delivery address.
- **Profile:** Shopper profile, order/saved/bag count tiles, saved delivery address management, app version info, and data clear action.

---

## 3. How to Build & Run

1. Open the project in **Android Studio (Ladybug or newer)** with **JDK 17**.
2. Let Gradle sync dependencies.
3. Select an emulator or physical device running **Android 8.0+ (API 26+)**.
4. Run:
   ```bash
   ./gradlew assembleDebug       # Build debug APK
   ./gradlew testDebugUnitTest   # Run unit tests
   ./gradlew assembleRelease     # Build minified production release APK
   ```

## 5. CI/CD

GitHub Actions runs the unit tests and builds the debug APK on every push and pull request. The debug APK is available as a workflow artifact.

Pushing a version tag such as `v1.0.0` runs the release workflow and publishes the release APK to GitHub Releases. The APK is unsigned; configure Android signing credentials before distributing an installable production build.

---

## 6. Security & ProGuard Configuration

In `app/proguard-rules.pro`:
- Keeps reflectively parsed Retrofit & Gson models: `com.vastra.store.data.model.**`
- Keeps DataStore persistent models: `com.vastra.store.data.local.**`
- Keeps UI enum options: `com.vastra.store.ui.SortOption`
- Preserves `@SerializedName` annotations and field signatures for safe R8 obfuscation.
