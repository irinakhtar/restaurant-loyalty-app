# Regulars

Regulars is a Kotlin and Jetpack Compose loyalty app: a restaurant list, an 8-stamp card, reward rules, favourites, and an offline cache.

Sprint 1 is the project shell. It installs as `com.regulars` and opens on a screen that says **All restaurants**. Reward rules and the mock restaurant contract are already covered by unit tests. The list, stamp card, and settings screens come in later sprints.

## Prerequisites

- JDK 17
- Android SDK, with `local.properties` containing `sdk.dir` (Android Studio writes this when you open the project)
- An emulator or a device on API 26 or higher

## Run

Open this folder in Android Studio (the one that contains `settings.gradle.kts`), wait for Gradle sync, start an emulator, and press Run. The launcher activity is `com.regulars.MainActivity`.

From the project root in PowerShell, with an emulator already booted or a device connected (`adb devices` shows `device`):

```powershell
.\gradlew.bat :app:installDebug
adb shell am start -n com.regulars/.MainActivity
```

On macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`.

## Test

Unit tests do not need a device:

```powershell
.\gradlew.bat test
```

| What | Where |
| --- | --- |
| Reward rules (8 stamps, claim resets the card, one stamp per restaurant per hour) | `app/src/main/java/com/regulars/domain/RewardRules.kt` |
| Tests for those rules | `app/src/test/java/com/regulars/domain/RewardRulesTest.kt` |
| Mock JSON the screens will load | `app/src/main/assets/restaurants.json` |
| Contract test for that JSON | `app/src/test/java/com/regulars/data/RestaurantContractTest.kt` |

The HTML report is written to `app/build/reports/tests/testDebugUnitTest/index.html`.

GitHub Actions runs `./gradlew test` on every push and pull request (`.github/workflows/unit-tests.yml`).

Style check:

```powershell
.\gradlew.bat ktlintCheck
```
