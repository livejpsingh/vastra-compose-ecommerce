# Developer guide

## Prerequisites

- Android Studio with Android SDK Platform 34.
- JDK 17.
- Android emulator or device at API 26 or higher for running the app.

The project uses the checked-in Gradle wrapper. Let the wrapper download its configured Gradle distribution and let Android Studio sync dependencies before the first build.

## Build and test

macOS or Linux:

```shell
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew assembleRelease
```

Windows PowerShell:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

The unit-test report is generated under `app/build/reports/tests/`. APK outputs are under `app/build/outputs/apk/`. The release variant is minified and unsigned.

To run the application, open the project in Android Studio, wait for Gradle sync, select an API 26+ emulator/device, and run the `app` configuration.

## Repository layout

| Path | Responsibility |
| --- | --- |
| `app/src/main/java/com/vastra/store/MainActivity.kt` | Android activity and Compose entry point. |
| `app/src/main/java/com/vastra/store/VastraApplication.kt` | App container initialization and Coil configuration. |
| `app/src/main/java/com/vastra/store/data/` | Dependency container, repository, models, local store, and remote API. |
| `app/src/main/java/com/vastra/store/ui/` | ViewModel, UI state, navigation, shared components, screens, and theme. |
| `app/src/main/java/com/vastra/store/util/` | Pricing, address validation, category, size, and formatting helpers. |
| `app/src/test/` | Local JVM unit tests. |
| `.github/workflows/` | Android CI and tagged GitHub Release workflows. |
| `docs/` | Architecture and developer documentation. |

## Making common changes

### Add or change a screen

1. Implement the screen in `ui/screens/` using state values and callbacks as inputs.
2. Add or update its route and screen wiring in `ui/VastraApp.kt`.
3. Put shared UI elements in `ui/components/` and use the existing Material theme.
4. Keep business behavior in `ShopViewModel` or a suitable data/util layer rather than embedding stateful workflows in a screen.

### Change app state or persisted data

1. Update the relevant model in `data/model/` or `data/local/`.
2. Add the DataStore read/write flow to `ShopStore` when a new value must survive process restarts.
3. Expose the change through `ShopViewModel` and its `ShopUiState`.
4. Wire state and event callbacks through `VastraApp` to the appropriate screen.
5. Add or update a unit test for the changed calculation or model behavior.

Shopper state is stored locally; adding a backend or account system requires a separate API/authentication design and is outside the current app behavior.

### Change catalogue networking

Update the Retrofit endpoint in `data/remote/ShopApi.kt`, and update repository mapping/caching in `data/ShopRepository.kt` if the response or supported categories change. Keep API-specific response types in `data/model/Models.kt` or dedicated data models as appropriate.

## Tests and CI

The current unit tests exercise pricing, shipping thresholds, address validation, product discounts, stock state, and cart line totals. Run `testDebugUnitTest` locally before pushing.

The Android CI workflow runs unit tests and builds the debug APK on pushes and pull requests. The Android CD workflow runs for tags matching `v*` and attaches an unsigned release APK to a GitHub Release. Configure stable Android signing credentials before publishing an installable production APK.

## Local configuration and secrets

Do not commit `local.properties`, signing keys, passwords, or API credentials. Local Android SDK paths belong in `local.properties`, which is ignored by Git. The current catalogue endpoint does not require an API key.
