# OpenSchoolCloud Calendar

**De eenvoudige agenda-app voor scholen, bovenop Nextcloud.**

🇳🇱 🇧🇪 🇩🇪 🇫🇷 — *Gebouwd voor Europees onderwijs*

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](#android)
[![iOS](https://img.shields.io/badge/Platform-iOS-lightgrey.svg)](#ios)

---

## Huidige Status (v0.1.0-alpha)

**Android:** 🟡 In actieve ontwikkeling (Sprint 2.5 compleet)

| Feature | Status | Notities |
|---------|--------|----------|
| Login & Onboarding | ✅ Werkt | 3-velden login, credential encryption |
| CalDAV Discovery | ✅ Werkt | Well-known, principal, calendar-home-set |
| Account Opslag | ✅ Werkt | EncryptedSharedPreferences |
| Calendar Sync | ✅ Werkt | CTag-based differential sync |
| Week View | ✅ Werkt | 7-kolommen grid met events |
| Day View | ✅ Werkt | Uur-voor-uur weergave |
| Month View | 🟡 Basis | Grid aanwezig, navigatie werkt |
| Pull-to-Refresh | ✅ Werkt | Handmatige sync trigger |
| Background Sync | ✅ Werkt | WorkManager, 15 min interval |
| Offline Cache | ✅ Werkt | Room database |
| Event Details | 🔴 TODO | UI stub aanwezig |
| Event Create/Edit | 🔴 TODO | UI stub aanwezig |
| Huisstijl | ✅ Werkt | OSC brand colors, Dutch strings |
| Splash Screen | ✅ Werkt | SplashScreen API |

**iOS:** ⬜ Nog niet gestart

---

## Het Probleem

Scholen willen weg van Google Calendar, maar het alternatief (Nextcloud + DAVx⁵) is te technisch:

```
Huidige situatie:
1. Installeer Nextcloud app
2. Installeer DAVx⁵ (wat is dat?)
3. Configureer CalDAV URL
4. Maak app-wachtwoord aan
5. Synchroniseer met system calendar
6. Open een andere agenda-app

→ Resultaat: "Ik gebruik gewoon Google Calendar"
```

## De Oplossing

```
OpenSchoolCloud Calendar:
1. Installeer de app
2. Vul in: URL + gebruikersnaam + app-wachtwoord
3. Klaar — je agenda werkt
```

---

## Features

### MVP (in ontwikkeling)
- ✅ One-time onboarding (3 velden, 30 seconden)
- ✅ Week view met events
- ✅ Day view met uur-voor-uur weergave
- 🟡 Month view (basis, navigatie werkt)
- 🔴 Events aanmaken en bewerken (TODO)
- 🔴 Event details weergave (TODO)
- 🔴 Uitnodigingen versturen (TODO)
- 🔴 Reminders/notificaties (TODO)
- ✅ Offline cache (Room database)
- ✅ Kalenderkleur support
- ✅ Nederlandse UI
- ✅ Pull-to-refresh sync
- ✅ Background sync (WorkManager)

### v1 (gepland)
- ⬜ Meerdere accounts
- ⬜ Zoekfunctie
- ⬜ Herhalende afspraken (volledige edit)
- ⬜ Widgets (Android + iOS)
- ⬜ Contact autocomplete (device + CardDAV)

### v2 (gepland)
- ⬜ Natural language input
- ⬜ Free/busy scheduling assistant
- ⬜ 10-minutengesprekken integratie

Zie [SCOPE.md](SCOPE.md) voor de volledige specificatie.

---

## Platforms

### Android

**Stack:** Kotlin, Jetpack Compose, Room, WorkManager

**Minimum:** Android 8.0 (API 26)

```bash
cd android/
./gradlew assembleDebug
```

### iOS

**Stack:** Swift, SwiftUI, CoreData, BackgroundTasks

**Minimum:** iOS 15.0

```bash
cd ios/
open OpenSchoolCloudCalendar.xcodeproj
# Of via xcodebuild
```

---

## Development

### Prerequisites

**Android:**
- Android Studio Hedgehog (2023.1.1) of nieuwer
- JDK 17

**iOS:**
- Xcode 15+
- macOS Sonoma of nieuwer

### Getting Started

```bash
git clone https://github.com/NickAldewereld/openschoolcloud-calendar.git
cd openschoolcloud-calendar

# Android
cd android/
./gradlew build

# iOS
cd ios/
pod install  # indien CocoaPods dependencies
open OpenSchoolCloudCalendar.xcworkspace
```

### Project Structure

```
openschoolcloud-calendar/
├── android/                              # Android app (Kotlin/Compose)
│   ├── app/src/main/java/nl/openschoolcloud/calendar/
│   │   ├── data/                        # Data layer
│   │   │   ├── local/                   # Room database, DAOs
│   │   │   │   └── entity/              # EventEntity, CalendarEntity, etc.
│   │   │   └── remote/                  # CalDAV client, XML parser
│   │   │       └── auth/                # Credential storage
│   │   ├── domain/                      # Domain layer
│   │   │   ├── model/                   # Event, Calendar, Account models
│   │   │   ├── repository/              # Repository interfaces
│   │   │   └── usecase/                 # Business logic
│   │   ├── presentation/                # UI layer
│   │   │   ├── calendar/                # Calendar screens (week/day/month)
│   │   │   ├── login/                   # Login screen
│   │   │   ├── settings/                # Settings screen
│   │   │   ├── navigation/              # App navigation
│   │   │   └── theme/                   # Material3 theme, colors
│   │   ├── di/                          # Hilt dependency injection
│   │   └── sync/                        # WorkManager background sync
│   ├── app/src/test/                    # Unit tests (~73 tests)
│   └── build.gradle.kts
│
├── ios/                                 # iOS app (nog niet gestart)
├── shared/                              # Shared documentation
├── SCOPE.md                             # Feature scope
├── MILESTONES.md                        # Sprint planning
├── CHANGELOG.md                         # Wijzigingen per sprint
└── README.md
```

---

## CalDAV Implementation Notes

De app communiceert direct met Nextcloud via CalDAV. Geen tussenlaag, geen eigen backend.

**Discovery flow:**
1. User geeft server URL
2. App doet PROPFIND op `/.well-known/caldav` of `/remote.php/dav/`
3. Discover `current-user-principal`
4. Discover `calendar-home-set`
5. List calendars

**Sync strategy:**
- CTag-based differential sync
- Server is single source of truth
- Offline changes queued, sync on reconnect

Zie [shared/caldav/](shared/caldav/) voor protocol details.

---

## Privacy & Security

- **Geen analytics** — geen Firebase, geen tracking
- **Geen telemetrie** — tenzij opt-in
- **Credentials encrypted** — Android Keystore / iOS Keychain
- **Minimale permissies** — Internet, Notifications, Contacts (optioneel)
- **Open source** — audit zelf de code

---

## Contributing

Zie [CONTRIBUTING.md](CONTRIBUTING.md) voor guidelines.

**We zoeken:**
- Android developers (Kotlin/Compose)
- iOS developers (Swift/SwiftUI)
- CalDAV/iCalendar expertise
- Vertalers (DE, FR, ES)
- Testers

---

## License

This project is licensed under the **GNU Affero General Public License v3.0** — zie [LICENSE](LICENSE).

You are free to use, modify, and distribute this software, but any modifications
must also be open sourced under AGPL v3.

The "OpenSchoolCloud" name and branding are trademarks and may not be used
for competing services without permission. Zie [NOTICE](NOTICE) voor details.

```
Copyright 2025 OpenSchoolCloud / Aldewereld Consultancy
```

---

## Links

- **Website:** [openschoolcloud.nl](https://openschoolcloud.nl)
- **Issues:** [GitHub Issues](https://github.com/NickAldewereld/openschoolcloud-calendar/issues)
- **Contact:** info@openschoolcloud.nl

---

<p align="center">
  <strong>OpenSchoolCloud Calendar</strong><br>
  <em>Jullie school, jullie agenda. In Europa.</em>
</p>
