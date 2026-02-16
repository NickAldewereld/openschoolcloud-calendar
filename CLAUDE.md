# CLAUDE.md — OSC Calendar Development Guide

## 🏗️ Project Identity

**App:** OSC Calendar (OpenSchoolCloud Calendar)
**Package:** `nl.openschoolcloud.calendar`
**Platform:** Android (Kotlin, Jetpack Compose, Material3)
**License:** AGPL v3 (consistent with Nextcloud)
**Repository:** github.com/OpenSchoolCloud/openschoolcloud-calendar
**Owner:** Aldewereld Consultancy (trade name: OpenSchoolCloud)

**Mission:** Privacy-first kalender app voor Nederlands onderwijs die synchroniseert met Nextcloud via CalDAV. Niet alleen een planning-tool maar een pedagogisch instrument voor "leren leren".

---

## 📐 Architecture & Conventions

### Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 1.9.x |
| Compose BOM | 2024.12.01 |
| Material3 | Via BOM |
| Room | 2.6.x |
| Hilt | 2.48+ |
| OkHttp | 4.12.x |
| ical4j | 3.2.14 |
| ZXing | 3.5.2 |
| WorkManager | 2.9.0 |
| minSdk | 26 (Android 8.0) |
| targetSdk | 34 (Android 14) |
| compileSdk | 35 |

### Architecture Pattern: MVVM + Clean Architecture

```
domain/          ← Pure Kotlin, no Android deps
  model/         ← Data classes (Event, Calendar, Account, BookingConfig)
  repository/    ← Interfaces only
  usecase/       ← Business logic

data/            ← Android-specific implementations
  local/         ← Room DB, DAOs, SharedPreferences
  remote/        ← CalDAV client, Nextcloud OCS API
  repository/    ← *RepositoryImpl classes
  sync/          ← WorkManager sync workers

presentation/    ← Compose UI
  screens/       ← Feature-based folders (calendar/, event/, booking/, etc.)
  components/    ← Shared composables
  navigation/    ← NavGraph.kt
  theme/         ← Colors.kt, Theme.kt, Typography.kt

di/              ← Hilt modules (AppModule.kt)
notification/    ← Reminders, AlarmManager, WorkManager backup
widget/          ← Home screen widgets (Today, NextEvent)
util/            ← QR code generator, helpers
```

### Naming & Style Rules

- **Language:** Kotlin only. No Java files.
- **UI language:** Nederlands (Dutch) for all user-facing strings. Use `strings.xml`.
- **Code language:** English for all code, comments, and variable names.
- **Compose:** Stateless composables preferred. State hoisted to ViewModel.
- **DI:** Hilt for all dependency injection. `@HiltViewModel`, `@Inject constructor`.
- **Navigation:** Compose Navigation with sealed class routes in `NavGraph.kt`.
- **Database:** Room with TypeConverters. `exportSchema = false`.
- **Async:** Kotlin Coroutines + Flow. No RxJava.
- **Preferences:** `AppPreferences.kt` wrapper around SharedPreferences (with EncryptedSharedPreferences fallback).
- **Time:** `java.time.Instant` and `java.time.LocalDate` only. No `java.util.Date`.

### Branding Constants

```kotlin
// Primary: #3B9FD9 (OSC Blue)
// App name: "OSC Calendar"
// Tagline: "Jouw agenda, jouw privacy"
// Footer: "Powered by OpenSchoolCloud.nl"
```

### File Header (Required on ALL .kt files)

```kotlin
/*
 * OSC Calendar - Privacy-first calendar for Dutch education
 * Copyright (C) 2025 Aldewereld Consultancy (OpenSchoolCloud)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 */
```

---

## 🔒 Closed-Loop Development Rules

**Every sprint MUST produce a releasable build.** This is non-negotiable.

### Before ANY Code Change

1. **Read the relevant existing files first.** Understand the current implementation before modifying.
2. **Check imports and dependencies.** Don't add libraries without verifying they're compatible with the BOM and minSdk 26.
3. **Check the Known Issues section** below for traps already encountered.

### During Development

4. **One feature = one commit.** Atomic commits with descriptive messages in format: `Sprint X: [Feature description]`
5. **Every new screen** gets:
   - A ViewModel with `@HiltViewModel`
   - A route in `NavGraph.kt`
   - An entry in `strings.xml` for all user-facing text
6. **Every new data entity** gets:
   - A Room Entity + DAO
   - A domain model in `Models.kt`
   - A repository interface + implementation
   - Hilt bindings in `AppModule.kt`
7. **Every new feature** must be behind a feature flag in `AppPreferences.kt` if it affects existing functionality.

### After Each Feature

8. **Build verification:** Run `./gradlew assembleDebug` and confirm it succeeds with 0 errors.
9. **Lint check:** Run `./gradlew lint` and fix any errors (warnings acceptable for now).
10. **Manual smoke test checklist:**
    - App launches without crash
    - Login flow works
    - CalDAV sync completes
    - New feature is accessible and functional
    - Back navigation works correctly
    - Dark mode doesn't break layout

### Sprint Completion (Release Gate)

11. **Version bump:** Update `versionCode` (increment by 1) and `versionName` in `build.gradle.kts`
12. **Build release APK:** `./gradlew assembleRelease`
13. **Generate signed APK/AAB** for Play Store upload
14. **Update CHANGELOG.md** with sprint summary
15. **Git tag:** `git tag -a vX.Y.Z -m "Sprint N: summary"`

---

## ⚠️ Known Issues & Traps (Must Read!)

These bugs have been encountered and fixed. Do NOT reintroduce them:

| Issue | Root Cause | Solution |
|-------|-----------|----------|
| `PullToRefreshBox` not found | Not in Compose BOM 2024.12.01 | Don't use it. Use custom pull-to-refresh or SwipeRefresh |
| `HorizontalDivider` not found | Material3 version mismatch | Use `Divider` instead |
| `when` not exhaustive | Sealed classes without `else` | Always add `else ->` branch |
| META-INF packaging conflict | ical4j vs groovy-dateutil | Keep packaging excludes in build.gradle.kts |
| EncryptedSharedPreferences crash | MasterKey fails on some devices/emulators | Use lazy init + fallback to regular SharedPrefs |
| CalDAV relative URL | Nextcloud returns `/remote.php/dav/...` without scheme | Always use `resolveUrl()` against server origin |
| Widget crash | Receivers not exported | All widget receivers need `android:exported="true"` |
| KeyframesSpec NoSuchMethodError | Compose BOM version too old | Stay on BOM 2024.12.01 or newer |

### Dependency Rules

- **DO NOT** downgrade Compose BOM below `2024.12.01`
- **DO NOT** add Jetpack libraries outside the BOM unless absolutely necessary
- **DO NOT** use `LocalDateTime` for storage — always `Instant` (UTC)
- **DO NOT** use `java.util.Calendar` anywhere — use `java.time.*`
- **DO NOT** add Google Play Services dependencies (privacy-first principle)

---

## 📊 Current State (Post Sprint 3)

### ✅ Completed Features

**Sprint 1:** CalDAV sync, Room database, Hilt DI, login flow, basic calendar UI
**Sprint 2:** Event details, theme/branding, calendar view with week numbers, quick capture, create/edit events, settings
**Sprint 3:** AGPL v3 license, push notifications, Today widget, Next Event widget, booking links + QR codes, privacy URLs

### 🔧 Open Hotfixes (Do These First in Next Sprint)

1. App icon → Must use OSC logo (`osc_logo.png`) in all `mipmap-*` directories
2. App name → Change to "OSC Calendar" in `strings.xml` (currently "OpenSchoolCloud Agenda")
3. Widget receivers → Add `android:exported="true"` in `AndroidManifest.xml`
4. `TodayWidgetService` → Add try-catch around database access
5. In-app upsell → Promo card for OpenSchoolCloud.nl hosting (TODO)

---

## 🗺️ Feature Roadmap (Sprint 4+)

Features are ordered by: (1) releasability value, (2) user impact, (3) technical dependency.
Each sprint should be completable in 1 coding session (3-5 hours) and produce a shippable APK.

### Sprint 4: Hotfixes + Play Store Ready 🏪

**Goal:** Fix all open issues, prepare Play Store listing, first public release.

Tasks:
- [ ] Fix app icon (all mipmap densities)
- [ ] Fix app name to "OSC Calendar"
- [ ] Fix widget `exported="true"`
- [ ] Fix TodayWidgetService try-catch
- [ ] Add Play Store metadata: feature graphic, screenshots, description (NL + EN)
- [ ] Verify ProGuard/R8 rules for release build
- [ ] Add crash reporting (open source: ACRA or similar, NO Firebase/Google)
- [ ] Add onboarding screen (3 slides: privacy, CalDAV, features)
- [ ] Version: 1.0.0

### Sprint 5: Multiculturele Feestdagenkalender 🌍

**Goal:** Subscribable calendar feeds with Dutch and international holidays/celebrations.

Architecture:
```
domain/model/
  HolidayCalendar.kt        ← data class (name, category, events, enabled)
  HolidayEvent.kt           ← data class (title, date, description, culture, ageGroup)

data/local/
  HolidayDao.kt             ← Room DAO
  holiday_data/              ← JSON seed files per culture

data/remote/
  HolidayFeedClient.kt      ← Optional: fetch updated feeds from OSC server

presentation/screens/
  holidays/
    HolidayDiscoverScreen.kt  ← Browse available calendars
    HolidayDetailSheet.kt     ← Bottom sheet: "Wat vieren we?"
    HolidayViewModel.kt
```

Calendar categories:
- 🇳🇱 Nederlandse feestdagen (Koningsdag, Bevrijdingsdag, Sinterklaas, etc.)
- ☪️ Islamitische feestdagen (Eid al-Fitr, Eid al-Adha, Ramadan, Mawlid)
- ✝️ Christelijke feestdagen (Pasen, Kerst, Hemelvaart, Pinksteren)
- ✡️ Joodse feestdagen (Pesach, Jom Kippur, Chanoeka, Rosh Hashana)
- 🕉️ Hindoestaanse feestdagen (Diwali, Holi, Phagwa)
- 🌏 Chinese/Aziatische feestdagen (Chinees Nieuwjaar, Maanfestival)
- 🌐 Internationale dagen (Kinderrechtendag, Werelddocentendag, etc.)

Each event includes:
- `title`: Naam van de feestdag
- `description`: Kindvriendelijke uitleg (max 200 woorden, leesniveau groep 5-6)
- `classroomTip`: Optionele gespreksstarter voor in de klas
- `culture`: Welke cultuur/religie
- `dateCalculation`: Fixed date of calculated (Islamic calendar, Chinese calendar, Easter-based)

Display: Colored dots on calendar view per category. Tap to see detail sheet.
Settings: Per-category toggle (default: Nederlandse feestdagen ON, rest OFF).

**Privacy note:** Calendar selections are stored locally only. Never synced to server. No tracking of which cultures a school subscribes to.

### Sprint 6: Leerling-Agenda & Reflectie 📝

**Goal:** Transform event description into a structured "learning agenda" with optional reflection.

Architecture:
```
domain/model/
  LearningAgenda.kt          ← data class extending Event with pedagogical fields
  ReflectionEntry.kt         ← data class (eventId, mood, whatWentWell, whatToDoBetter, timestamp)

data/local/
  ReflectionDao.kt

presentation/screens/
  reflection/
    ReflectionPromptSheet.kt  ← Bottom sheet after event ends
    WeekReviewScreen.kt       ← Weekly overview of reflections
    ReflectionViewModel.kt

presentation/components/
  MoodSelector.kt             ← 5 emoji mood picker (😫😕😐🙂🤩)
  LearningGoalInput.kt        ← "Wat ga ik doen? Wat heb ik nodig?"
```

Features:
- When creating an event, optionally toggle "Leeragenda" mode
- Description field becomes structured: "Wat ga ik doen?" / "Wat heb ik nodig?"
- Post-event notification: "Hoe ging [event title]?" → opens ReflectionPromptSheet
- Reflection is lightweight: mood emoji + 1 optional sentence
- WeekReviewScreen: horizontal timeline of moods + highlights
- Leraar-view: aggregated mood overview per klas (if shared calendar)

**Pedagogisch kader:** Zimmerman's Self-Regulated Learning model (forethought → performance → self-reflection). Validated by SLO's "leren leren" framework.

### Sprint 7: Week Vooruit Planner 🗓️

**Goal:** Guided weekly planning flow for students.

Features:
- Monday morning (or configurable) notification: "Plan je week!"
- Guided flow: "Wat zijn je 3 belangrijkste taken deze week?"
- Drag-and-drop tasks onto days
- Tasks appear as light-colored blocks in calendar
- End-of-week review: "Hoeveel taken heb je afgerond?" → progress visualization
- Streak counter: "Je plant al 4 weken op rij!" 🔥

Architecture: Reuse existing Event creation with a new `eventType: TASK` field. Tasks are stored as regular CalDAV events with a custom X-OSC-TYPE property (CalDAV compatible, syncs to Nextcloud).

### Sprint 8: Seizoenskaarten & Ontdek-Feed 🌱

**Goal:** Contextual discovery cards tied to the school year and seasons.

Content categories:
- Natuur & Seizoenen: "De bomen verkleuren — waarom eigenlijk?"
- Schooljaar-momenten: "Tips voor je eerste spreekbeurt", "Cito-week: zo bereid je je voor"
- Wereldoriëntatie: Linked to upcoming holidays from Sprint 5
- Fun facts: "Vandaag in de geschiedenis..."

Architecture:
```
presentation/components/
  DiscoverCard.kt             ← Material3 Card with image, title, snippet
  DiscoverFeed.kt             ← Lazy column of cards

data/local/
  discover_content/           ← JSON seed files, organized by month
```

Cards shown on calendar's daily view when there's space. Dismissable, not intrusive.
Content is bundled in APK (offline-first). Optional remote updates via OSC server.

### Sprint 9: Samenwerkingsblokken 🤝

**Goal:** Group events where multiple students work together on projects.

Features:
- Create "Groepsproject" event type
- Invite classmates (from shared calendar)
- Subtask assignment within the event
- Shared description/notes (via CalDAV, all participants see updates)
- Teacher can see all group events in class overview

### Sprint 10: AI Planning Coach 🤖

**Goal:** Optional, privacy-respectful AI assistant that helps with planning (NOT answering questions).

Features:
- Analyzes upcoming events and suggests study schedule
- "Je hebt 3 toetsen volgende week. Wil je een studieplan?"
- Suggests time blocks based on past reflection data
- Detects overloaded days: "Woensdag ziet er druk uit. Wil je iets verplaatsen?"
- Runs 100% on-device (no cloud AI calls) using simple heuristics first
- Future: optional Nextcloud AI integration (Nextcloud Assistant API)

**Privacy principle:** All AI processing happens locally. No student data leaves the device for AI purposes. Ever.

---

## 🧪 Testing Strategy

### Minimum Viable Testing (Every Sprint)

```bash
# 1. Build succeeds
./gradlew assembleDebug

# 2. Lint passes (errors only)
./gradlew lint

# 3. Unit tests pass (when present)
./gradlew testDebugUnitTest
```

### Smoke Test Checklist (Manual, Every Release)

```
[ ] Fresh install on Android 8.0 (API 26) emulator
[ ] Fresh install on Android 14 (API 34) emulator
[ ] Login with Nextcloud server
[ ] CalDAV sync completes without error
[ ] Create event → syncs to server
[ ] Edit event → changes reflected
[ ] Delete event → removed from server
[ ] Widgets load on home screen
[ ] Notifications fire at correct time
[ ] Dark mode: no invisible text, proper contrast
[ ] Landscape mode: no layout breakage
[ ] Back button: no unexpected behavior
[ ] App survives process death (background kill + restore)
[ ] [New feature specific tests]
```

### Instrumented Tests (Target: Sprint 6+)

```
androidTest/
  CalDavSyncTest.kt          ← Mock server responses
  RoomMigrationTest.kt       ← Database version upgrades
  NavigationTest.kt          ← Compose navigation flows
```

---

## 📦 Release Process

### Versioning: Semantic Versioning

```
MAJOR.MINOR.PATCH
1.0.0  = Sprint 4 (first Play Store release)
1.1.0  = Sprint 5 (holiday calendars)
1.2.0  = Sprint 6 (reflections)
...
2.0.0  = Major redesign or breaking change
```

`versionCode` increments by 1 for every release (Play Store requires this).

### Release Channels

1. **Internal testing** (APK direct download via GitHub Releases)
2. **Closed testing** (Play Store, invite-only track)
3. **Open testing** (Play Store, public beta)
4. **Production** (Play Store, full release)

### Play Store Checklist

```
[ ] Signed AAB with upload key
[ ] Feature graphic (1024x500)
[ ] App icon (512x512)
[ ] Screenshots: phone (min 2), tablet (optional)
[ ] Short description (NL + EN, max 80 chars)
[ ] Full description (NL + EN, max 4000 chars)
[ ] Privacy policy URL: https://openschoolcloud.nl/juridisch/privacy
[ ] Content rating questionnaire completed
[ ] Target audience: NOT "children" (avoids COPPA/teacher-approved requirements)
[ ] Category: Productivity or Education
[ ] Contact email: configured
```

### APK Distribution (Parallel to Play Store)

For testers and schools that prefer sideloading:
- GitHub Releases: signed APK attached to each tag
- openschoolcloud.nl/download: direct APK link
- QR code on website pointing to latest APK

---

## 🔐 Privacy & Compliance Principles

These rules apply to ALL code:

1. **No Google Play Services.** No Firebase, no Google Analytics, no AdMob. Period.
2. **No tracking.** No usage analytics sent anywhere unless user explicitly opts in.
3. **No cloud AI.** All intelligent features run on-device.
4. **CalDAV standard only.** Custom properties use `X-OSC-` prefix and remain CalDAV compatible.
5. **Data minimization.** Collect only what's needed. Holiday calendar choices, reflections, and mood data stay on-device unless user explicitly shares.
6. **Transparent sync.** User always knows what syncs to Nextcloud and what stays local.
7. **No ads, no upsell popups.** The in-app promo for OpenSchoolCloud.nl hosting is a subtle card in Settings, not a popup.

---

## 💡 Development Tips

### Adding a New Screen

```kotlin
// 1. Create the screen composable
@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) { ... }

// 2. Create the ViewModel
@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val repository: SomeRepository
) : ViewModel() { ... }

// 3. Add route to NavGraph.kt
composable("new_feature") {
    NewFeatureScreen(onNavigateBack = { navController.popBackStack() })
}

// 4. Add strings to strings.xml
<string name="new_feature_title">Nieuwe Feature</string>

// 5. Add Hilt bindings if new repository
@Provides @Singleton
fun provideNewRepository(impl: NewRepositoryImpl): NewRepository = impl
```

### Adding a CalDAV Custom Property

```kotlin
// Use X-OSC- prefix for all custom properties
// These survive CalDAV sync and are ignored by other clients
val PROP_EVENT_TYPE = "X-OSC-EVENT-TYPE"    // TASK, LEARNING, GROUP
val PROP_REFLECTION = "X-OSC-REFLECTION"     // JSON blob
val PROP_MOOD = "X-OSC-MOOD"                 // 1-5
```

### Room Database Migration

```kotlin
// ALWAYS create a migration when adding/changing tables
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE events ADD COLUMN event_type TEXT DEFAULT 'STANDARD'")
    }
}

// Register in AppDatabase
.addMigrations(MIGRATION_X_Y)
```

---

## 📋 Sprint Execution Template

When starting a new sprint, follow this exact sequence:

```
1. HOTFIXES FIRST
   - Check "Open Hotfixes" section above
   - Fix all blocking issues before new features
   - Commit: "Hotfix: [description]"

2. FEATURE DEVELOPMENT
   - Read the sprint spec in the roadmap above
   - Create files following architecture pattern
   - Add strings to strings.xml (Dutch)
   - Add AGPL header to all new .kt files
   - Commit per feature: "Sprint X: [feature]"

3. INTEGRATION
   - Wire up navigation in NavGraph.kt
   - Add Hilt bindings in AppModule.kt
   - Test all existing features still work (regression)

4. QUALITY GATE
   - ./gradlew assembleDebug (must pass)
   - ./gradlew lint (no errors)
   - Manual smoke test
   - Version bump in build.gradle.kts

5. RELEASE
   - ./gradlew assembleRelease
   - Git commit: "Release vX.Y.Z"
   - Git tag: vX.Y.Z
   - Update CHANGELOG.md
```

---

*This document is the single source of truth for OSC Calendar development. Update it with each sprint.*
